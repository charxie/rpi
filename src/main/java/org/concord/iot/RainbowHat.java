package org.concord.iot;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.SoftPwm;
import org.concord.iot.drivers.AlphanumericDisplay;
import org.concord.iot.drivers.Apa102;
import org.concord.iot.drivers.Bmp280;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * This is an emulator of the Rainbow HAT for Raspberry Pi 3.
 *
 * @author Charles Xie
 */

public class RainbowHat {

    private static final int SENSOR_DATA_COLLECTION_INTERVAL = 1000; // milliseconds

    private GpioController gpio;
    private GpioPinDigitalInput buttonA;
    private GpioPinDigitalInput buttonB;
    private GpioPinDigitalInput buttonC;
    GpioPinDigitalOutput redLed;
    GpioPinDigitalOutput greenLed;
    GpioPinDigitalOutput blueLed;
    GpioPinDigitalOutput buzzer;
    Apa102 apa102;
    private Bmp280 bmp280;
    private AlphanumericDisplay display;
    String displayMode = "None";

    private DatabaseReference database;

    private long timeZeroMillis;
    private double currentTime;
    private double temperature;
    private double barometricPressure;
    boolean allowTemperatureTransmission;
    boolean allowBarometricPressureTransmission;
    private String alphanumericString = "----";

    RainbowHatBoardView boardView;
    RainbowHatGui gui;
    private ThreadPoolExecutor threadPool;
    private List<ThreadPoolListener> threadPoolListeners;

    private List<SensorDataPoint> temperatureDataStore;
    private List<SensorDataPoint> barometricPressureDataStore;

    public RainbowHat() {
        init();
    }

    private void init() {

        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        threadPoolListeners = new ArrayList<>();

        synchronizeWithCloud();

        Gpio.wiringPiSetup(); // initialize the wiringPi library, this is needed for PWM
        gpio = GpioFactory.getInstance();

        buttonA = gpio.provisionDigitalInputPin(RaspiPin.GPIO_29, "Button A");
        buttonB = gpio.provisionDigitalInputPin(RaspiPin.GPIO_28, "Button B");
        buttonC = gpio.provisionDigitalInputPin(RaspiPin.GPIO_27, "Button C");
        redLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_22, "Red LED", PinState.LOW);
        greenLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24, "Green LED", PinState.LOW);
        blueLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "Blue LED", PinState.LOW);
        buzzer = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23, "Buzzer", PinState.LOW);

        apa102 = new Apa102(RainbowHatState.NUMBER_OF_RGB_LEDS);
        //apa102.setDefaultRainbow(); // test

        try {
            display = new AlphanumericDisplay(AlphanumericDisplay.HT16K33.BLINK_OFF, AlphanumericDisplay.HT16K33.DUTY_01);
            display.displayOn();
            display.display(alphanumericString);
        } catch (Exception e) {
            display = null;
            e.printStackTrace();
        }

        try {
            bmp280 = new Bmp280(Bmp280.Protocol.I2C, Bmp280.ADDR_SDO_2_VDDIO, I2CBus.BUS_1);
            bmp280.setIndoorNavigationMode();
            bmp280.setMode(Bmp280.Mode.NORMAL, true);
            bmp280.setTemperatureSampleRate(Bmp280.Temperature_Sample_Resolution.TWO, true);
            bmp280.setPressureSampleRate(Bmp280.Pressure_Sample_Resolution.SIXTEEN, true);
            bmp280.setIIRFilter(Bmp280.IIRFilter.SIXTEEN, true);
            bmp280.setStandbyTime(Bmp280.Standby_Time.MS_POINT_5, true);
        } catch (Exception e) {
            e.printStackTrace();
            bmp280 = null;
        }

        setupButtons();
        startSensorDataCollection();

        temperatureDataStore = new ArrayList<>();
        barometricPressureDataStore = new ArrayList<>();

    }

    void submitTask(Runnable task) {
        int poolSize = threadPool.getCorePoolSize();
        if (threadPool.getActiveCount() >= poolSize) {
            threadPool.setCorePoolSize(poolSize + 1);
            notifyThreadPoolListeners();
        }
        threadPool.submit(task);
    }

    public void setNumberOfRgbLeds(int numberOfRgbLeds) {
        apa102.setNumberOfPixels(numberOfRgbLeds);
    }

    public int getNumberOfRgbLeds() {
        return apa102.getNumberOfPixels();
    }

    int getThreadPoolSize() {
        return threadPool.getCorePoolSize();
    }

    int getActiveThreadCount() {
        return threadPool.getActiveCount();
    }

    void addThreadPoolListener(ThreadPoolListener l) {
        if (!threadPoolListeners.contains(l))
            threadPoolListeners.add(l);
    }

    void removeThreadPoolListener(ThreadPoolListener l) {
        threadPoolListeners.remove(l);
    }

    void notifyThreadPoolListeners() {
        if (threadPoolListeners.isEmpty())
            return;
        ThreadPoolEvent e = new ThreadPoolEvent(this);
        for (ThreadPoolListener l : threadPoolListeners) {
            l.updated(e);
        }
    }

    void buzz(int k) {
        int a = buzzer.getPin().getAddress();
        SoftPwm.softPwmStop(a);
        switch (k) {
            case 1:
                SoftPwm.softPwmCreate(a, 0, 100);
                SoftPwm.softPwmWrite(a, 1);
                break;
            case 2:
                SoftPwm.softPwmCreate(a, 0, 1000);
                SoftPwm.softPwmWrite(a, 10);
                break;
            case 3:
                SoftPwm.softPwmCreate(a, 0, 100);
                SoftPwm.softPwmWrite(a, 100);
                break;
            default:
                SoftPwm.softPwmWrite(a, 0);
        }
    }

    public void touchA(boolean on) {
        if (on) {
            redLed.high();
            buzz(1);
        } else {
            redLed.low();
            buzz(0);
        }
        database.child("redLed").setValue(on, null);
        displayMode = "Temperature";
        database.child("displayMode").setValue(displayMode, null);
        updateDisplay();
        if (boardView != null) {
            boardView.setRedLedPressed(on);
        }
    }

    public void touchB(boolean on) {
        if (on) {
            greenLed.high();
            buzz(2);
        } else {
            greenLed.low();
            buzz(0);
        }
        database.child("greenLed").setValue(on, null);
        displayMode = "Pressure";
        database.child("displayMode").setValue(displayMode, null);
        updateDisplay();
        if (boardView != null) {
            boardView.setGreenLedPressed(on);
        }
    }

    public void touchC(boolean on) {
        if (on) {
            blueLed.high();
            buzz(3);
        } else {
            blueLed.low();
            buzz(0);
        }
        database.child("blueLed").setValue(on, null);
        if (boardView != null) {
            boardView.setBlueLedPressed(on);
        }
    }

    public void blinkRedLed(final int times, final int delay) {
        submitTask(() -> {
            for (int i = 0; i < times; i++) {
                try {
                    setRedLedState(true, true);
                    Thread.sleep(delay);
                    setRedLedState(false, true);
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                }
            }
        });
    }

    public void setRedLedState(boolean high, boolean updateRemote) {
        redLed.setState(high);
        buzz(high ? 1 : 0);
        if (boardView != null) {
            boardView.setRedLedPressed(high);
        }
        if (updateRemote) {
            database.child("redLed").setValue(high, null);
        }
    }

    public boolean getRedLedState() {
        return redLed.isHigh();
    }

    public void blinkGreenLed(final int times, final int delay) {
        submitTask(() -> {
            for (int i = 0; i < times; i++) {
                try {
                    setGreenLedState(true, true);
                    Thread.sleep(delay);
                    setGreenLedState(false, true);
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                }
            }
        });
    }

    public void setGreenLedState(boolean high, boolean updateRemote) {
        greenLed.setState(high);
        buzz(high ? 2 : 0);
        if (boardView != null) {
            boardView.setGreenLedPressed(high);
        }
        if (updateRemote) {
            database.child("greenLed").setValue(high, null);
        }
    }

    public boolean getGreenLedState() {
        return greenLed.isHigh();
    }

    public void blinkBlueLed(final int times, final int delay) {
        submitTask(() -> {
            for (int i = 0; i < times; i++) {
                try {
                    setBlueLedState(true, true);
                    Thread.sleep(delay);
                    setBlueLedState(false, true);
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                }
            }
        });
    }

    public void setBlueLedState(boolean high, boolean updateRemote) {
        blueLed.setState(high);
        buzz(high ? 3 : 0);
        if (boardView != null) {
            boardView.setBlueLedPressed(high);
        }
        if (updateRemote) {
            database.child("blueLed").setValue(high, null);
        }
    }

    public boolean getBlueLedState() {
        return blueLed.isHigh();
    }

    private void setupButtons() {
        GpioPinListenerDigital listener = event -> {
            GpioPin pin = event.getPin();
            PinState pinState = event.getState();
            System.out.println("GPIO Pin state change: " + pin + " = " + pinState);
            switch (pin.getPin().getAddress()) {
                case 29:
                    touchA(!pinState.isHigh());
                    break;
                case 28:
                    touchB(!pinState.isHigh());
                    break;
                case 27:
                    touchC(!pinState.isHigh());
                    break;
            }
        };
        buttonA.addListener(listener);
        buttonB.addListener(listener);
        buttonC.addListener(listener);
    }

    // TODO: Demical point
    private void updateDisplay() {
        if (display == null) {
            return;
        }
        alphanumericString = "----";
        if ("Temperature".equalsIgnoreCase(displayMode)) {
            alphanumericString = removeDot(Double.toString(temperature));
        } else if ("Pressure".equalsIgnoreCase(displayMode)) {
            alphanumericString = Long.toString(Math.round(barometricPressure));
        }
        if (alphanumericString.length() > 4) {
            alphanumericString = alphanumericString.substring(0, 4);
        } else if (alphanumericString.length() == 3) {
            alphanumericString = "0" + alphanumericString;
        } else if (alphanumericString.length() == 2) {
            alphanumericString = "00" + alphanumericString;
        } else if (alphanumericString.length() == 1) {
            alphanumericString = "000" + alphanumericString;
        } else if (alphanumericString.length() == 0) {
            alphanumericString = "----";
        }
        display.display(alphanumericString);
    }

    String getAlphanumericString() {
        return alphanumericString;
    }

    private static String removeDot(String s) {
        int i = s.indexOf('.');
        if (i != -1) {
            s = s.substring(0, i) + s.substring(i + 1);
        }
        return s;
    }

    public double getTime() {
        return currentTime;
    }

    private void startSensorDataCollection() {
        timeZeroMillis = System.currentTimeMillis();
        if (bmp280 == null) {
            return;
        }
        threadPool.execute(() -> {
            while (true) {
                try {
                    double[] results = bmp280.sampleDeviceReads();
                    temperature = results[Bmp280.TEMP_VAL_C];
                    barometricPressure = results[Bmp280.PRES_VAL];
                    System.out.printf("Temperature in Celsius : %.2f C %n", temperature);
                    System.out.printf("Pressure : %.2f hPa %n", barometricPressure);
                    currentTime = (double) (System.currentTimeMillis() - timeZeroMillis) / (double) SENSOR_DATA_COLLECTION_INTERVAL;
                    temperatureDataStore.add(new SensorDataPoint(currentTime, temperature));
                    barometricPressureDataStore.add(new SensorDataPoint(currentTime, barometricPressure));
                    updateDisplay();
                    if (allowTemperatureTransmission) {
                        database.child("temperature").setValue(temperature, null);
                    }
                    if (allowBarometricPressureTransmission) {
                        database.child("barometricPressure").setValue(barometricPressure, null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (boardView != null) {
                    boardView.repaint();
                }
                try {
                    Thread.sleep(SENSOR_DATA_COLLECTION_INTERVAL);
                } catch (InterruptedException e) {
                }
            }
        });
    }

    public void setAllowTemperatureTransmission(boolean b) {
        allowTemperatureTransmission = b;
        database.child("allowTemperatureTransmission").setValue(b, null);
    }

    public boolean getAllowTemperatureTransmission() {
        return allowTemperatureTransmission;
    }

    public double getTemperature() {
        return temperature;
    }

    public List<SensorDataPoint> getTemperatureDataStore() {
        return temperatureDataStore;
    }

    public void setAllowBarometricPressureTransmission(boolean b) {
        allowBarometricPressureTransmission = b;
        database.child("allowBarometricPressureTransmission").setValue(b, null);
    }

    public boolean getAllowBarometricPressureTransmission() {
        return allowBarometricPressureTransmission;
    }

    public double getBarometricPressure() {
        return barometricPressure;
    }

    public List<SensorDataPoint> getBarometricPressureDataStore() {
        return barometricPressureDataStore;
    }

    private void synchronizeWithCloud() {
        try {
            FileInputStream serviceAccount = new FileInputStream("raspberry-pi-java-firebase-adminsdk-eeeo1-f7e5dc2054.json");
            FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).setDatabaseUrl("https://raspberry-pi-java.firebaseio.com").build();
            FirebaseApp.initializeApp(options);
            database = FirebaseDatabase.getInstance().getReference("rainbow_hat");
            // database.setValue(new RainbowHatState(), null);
            database.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) { // This method is called once with the initial value and again whenever data at this location is updated.
                    DatabaseHandler.handle(RainbowHat.this, dataSnapshot);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    System.out.println("Failed to read value: " + error.toException());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void destroy() {
        try {
            if (!threadPool.isShutdown()) {
                threadPool.shutdownNow();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        buttonA.removeAllListeners();
        buttonB.removeAllListeners();
        buttonC.removeAllListeners();
        redLed.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        greenLed.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        blueLed.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        apa102.turnoff();
        if (display != null) {
            try {
                display.displayOff();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // stop all GPIO activity/threads by shutting down the GPIO controller (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        gpio.shutdown();
    }

    private void createAndShowGui() {
        boardView = new RainbowHatBoardView(this);
        gui = new RainbowHatGui();
        gui.createAndShowGui(this);
        boardView.addGraphListener(gui);
    }

    void setDefaultRainbow() {
        apa102.setDefaultRainbow();
        if (boardView != null) {
            for (int i = 0; i < RainbowHatState.NUMBER_OF_RGB_LEDS; i++) {
                boardView.setLedColor(i, apa102.getColor(i));
            }
        }
        ArrayList<ArrayList<Integer>> list = new ArrayList<>();
        for (int i = 0; i < RainbowHatState.NUMBER_OF_RGB_LEDS; i++) { // only save the state of the seven LEDs on the HAT
            list.add(getRgb(apa102.getColor(i)));
        }
        database.child("rainbowRgb").setValue(list, null); // TODO: There must be a way to set only the element of the list
    }

    private static ArrayList<Integer> getRgb(Color c) {
        ArrayList<Integer> rgb = new ArrayList<>(3);
        rgb.add(c.getRed());
        rgb.add(c.getGreen());
        rgb.add(c.getBlue());
        return rgb;
    }

    void chooseLedColor(Window parent, final int i) {
        Color c = JColorChooser.showDialog(parent, "LED1 Color", apa102.getColor(i));
        if (c != null) {
            apa102.setColor(i, c);
            if (boardView != null) {
                boardView.setLedColor(i, c);
            }
            ArrayList<ArrayList<Integer>> list = new ArrayList<>();
            for (int j = 0; j < RainbowHatState.NUMBER_OF_RGB_LEDS; j++) {
                list.add(i == j ? getRgb(c) : getRgb(apa102.getColor(j)));
            }
            database.child("rainbowRgb").setValue(list, null); // TODO: There must be a way to set only the element of the list
        }
    }

    public static void main(final String[] args) {

        final RainbowHat rainbowHat = new RainbowHat();

        if (GraphicsEnvironment.isHeadless()) {

            Scanner scanner = new Scanner(System.in);
            String line = "";
            while (!"q".equalsIgnoreCase(line)) {
                line = scanner.next();
                System.out.println("You typed: " + line);
            }
            scanner.close();
            rainbowHat.destroy();
            System.exit(0); // call this to exit and avoid a broken pipe error

        } else {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            EventQueue.invokeLater(() -> rainbowHat.createAndShowGui());
        }

    }

}