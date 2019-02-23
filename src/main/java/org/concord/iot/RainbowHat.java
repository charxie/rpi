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

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Charles Xie
 */

public class RainbowHat {

    private static final int SENSOR_DATA_COLLECTION_INTERVAL = 1000; // milliseconds

    private GpioController gpio;
    private GpioPinDigitalInput buttonA;
    private GpioPinDigitalInput buttonB;
    private GpioPinDigitalInput buttonC;
    private GpioPinDigitalOutput redLed;
    private GpioPinDigitalOutput greenLed;
    private GpioPinDigitalOutput blueLed;
    private GpioPinDigitalOutput buzzer;
    private Apa102 apa102;
    private Bmp280 bmp280;
    private AlphanumericDisplay display;
    private String displayMode = "None";

    private DatabaseReference database;

    private double temperature;
    private double barometricPressure;
    private boolean allowTemperatureTransmission;
    private boolean allowBarometricPressureTransmission;

    private RainbowHatBoardView boardView;

    public RainbowHat() {
        init();
    }

    private void init() {

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

        apa102 = new Apa102();
        //apa102.setColor(2, Color.BLUE); // test

        display = new AlphanumericDisplay(AlphanumericDisplay.HT16K33.BLINK_OFF, AlphanumericDisplay.HT16K33.DUTY_01);
        display.displayOn();
        display.display("----");

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
        }

        setupButtons();
        startSensorDataCollection();

    }

    private void buzz(int k) {
        SoftPwm.softPwmStop(buzzer.getPin().getAddress());
        switch (k) {
            case 1:
                SoftPwm.softPwmCreate(buzzer.getPin().getAddress(), 0, 100);
                SoftPwm.softPwmWrite(buzzer.getPin().getAddress(), 1);
                break;
            case 2:
                SoftPwm.softPwmCreate(buzzer.getPin().getAddress(), 0, 1000);
                SoftPwm.softPwmWrite(buzzer.getPin().getAddress(), 10);
                break;
            case 3:
                SoftPwm.softPwmCreate(buzzer.getPin().getAddress(), 0, 100);
                SoftPwm.softPwmWrite(buzzer.getPin().getAddress(), 100);
                break;
            default:
                SoftPwm.softPwmWrite(buzzer.getPin().getAddress(), 0);
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

    public void setRedLedState(boolean high) {
        redLed.setState(high);
    }

    public boolean getRedLedState() {
        return redLed.isHigh();
    }

    public void setGreenLedState(boolean high) {
        greenLed.setState(high);
    }

    public boolean getGreenLedState() {
        return greenLed.isHigh();
    }

    public void setBlueLedState(boolean high) {
        blueLed.setState(high);
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
        if (display != null) {
            String text = "----";
            if ("Temperature".equalsIgnoreCase(displayMode)) {
                text = removeDot(Double.toString(temperature));
            } else if ("Pressure".equalsIgnoreCase(displayMode)) {
                text = removeDot(Double.toString(barometricPressure));
            }
            if (text.length() > 4) {
                text = text.substring(0, 4);
            } else if (text.length() == 3) {
                text = "0" + text;
            } else if (text.length() == 2) {
                text = "00" + text;
            }
            display.display(text);
        }
    }

    private static String removeDot(String s) {
        int i = s.indexOf('.');
        if (i != -1) {
            s = s.substring(0, i) + s.substring(i + 1);
        }
        return s;
    }

    private void startSensorDataCollection() {
        Thread sensorThread = new Thread(() -> {
            while (true) {
                try {
                    double[] results = bmp280.sampleDeviceReads();
                    temperature = results[Bmp280.TEMP_VAL_C];
                    barometricPressure = results[Bmp280.PRES_VAL];
                    System.out.printf("Temperature in Celsius : %.2f C %n", temperature);
                    System.out.printf("Pressure : %.2f hPa %n", barometricPressure);
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
                try {
                    Thread.sleep(SENSOR_DATA_COLLECTION_INTERVAL);
                } catch (InterruptedException e) {
                }
            }
        });
        sensorThread.setPriority(Thread.MIN_PRIORITY);
        sensorThread.start();
    }

    public double getTemperature() {
        return temperature;
    }

    public double getBarometricPressure() {
        return barometricPressure;
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

                    System.out.println("Value changed: " + dataSnapshot.getValue());
                    RainbowHatState state = dataSnapshot.getValue(RainbowHatState.class);
                    allowTemperatureTransmission = state.allowTemperatureTransmission;
                    allowBarometricPressureTransmission = state.allowBarometricPressureTransmission;

                    redLed.setState(state.redLed);
                    greenLed.setState(state.greenLed);
                    blueLed.setState(state.blueLed);
                    if (state.redLed) {
                        buzz(1);
                    } else if (state.greenLed) {
                        buzz(2);
                    } else if (state.blueLed) {
                        buzz(3);
                    } else {
                        buzz(0);
                    }
                    for (int i = 0; i < RainbowHatState.NUMBER_OF_RGB_LEDS; i++) { // the led strip goes from the right to the left (0 is the rightmost and 6 is the leftmost).
                        ArrayList<Integer> rgb = state.rainbowRgb.get(i);
                        apa102.setColor(i, new Color(rgb.get(0), rgb.get(1), rgb.get(2)));
                    }

                    displayMode = state.displayMode;

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

    private void destroy() {
        buttonA.removeAllListeners();
        buttonB.removeAllListeners();
        buttonC.removeAllListeners();
        redLed.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        greenLed.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        blueLed.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        apa102.turnoff();
        display.displayOff();
        // stop all GPIO activity/threads by shutting down the GPIO controller (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        gpio.shutdown();
    }

    private void createAndShowGui() {

        final JFrame frame = new JFrame("Rainbow HAT Emulator on Raspberry Pi");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(100, 100);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                destroy();
                System.exit(0);
            }
        });

        JPanel contentPane = new JPanel(new BorderLayout(25, 25));
        frame.setContentPane(contentPane);
        boardView = new RainbowHatBoardView(this);
        contentPane.add(boardView, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        JButton button = new JButton("Exit");
        button.addActionListener(e -> {
            destroy();
            frame.dispose();
            System.exit(0);
        });
        buttonPanel.add(button);

        frame.pack();
        frame.setVisible(true);

    }

    void chooseLedColor(Window parent, final int i) {
        Color c = JColorChooser.showDialog(parent, "LED1 Color", apa102.getColor(i));
        if (c != null) {
            apa102.setColor(i, c);
            ArrayList<ArrayList<Integer>> list = new ArrayList<>();
            for (int j = 0; j < RainbowHatState.NUMBER_OF_RGB_LEDS; j++) {
                if (i == j) {
                    ArrayList<Integer> rgb = new ArrayList<>(3);
                    rgb.add(c.getRed());
                    rgb.add(c.getGreen());
                    rgb.add(c.getBlue());
                    list.add(rgb);
                } else {
                    ArrayList<Integer> rgb = new ArrayList<>(3);
                    Color c2 = apa102.getColor(j);
                    rgb.add(c2.getRed());
                    rgb.add(c2.getGreen());
                    rgb.add(c2.getBlue());
                    list.add(rgb);
                }
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
            EventQueue.invokeLater(() -> rainbowHat.createAndShowGui());
        }

    }

}