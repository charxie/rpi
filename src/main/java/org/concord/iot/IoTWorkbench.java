package org.concord.iot;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.component.temperature.impl.TmpDS18B20DeviceType;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.SoftPwm;
import org.concord.iot.drivers.*;
import org.concord.iot.listeners.ThreadPoolEvent;
import org.concord.iot.listeners.ThreadPoolListener;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.prefs.Preferences;

/**
 * @author Charles Xie
 */

public class IoTWorkbench {

    public final static String BRAND_NAME = "IoT Workbench";
    public final static String VERSION_NUMBER = "0.0.3";
    public final static byte RAINBOW_HAT = 0;
    public final static byte SENSOR_HUB = 1;

    private byte boardType = RAINBOW_HAT;

    private GpioController gpio;
    private GpioPinDigitalInput buttonA;
    private GpioPinDigitalInput buttonB;
    private GpioPinDigitalInput buttonC;
    private GpioPinDigitalInput pirMotionSensor;
    GpioPinDigitalOutput redLed;
    GpioPinDigitalOutput greenLed;
    GpioPinDigitalOutput blueLed;
    GpioPinDigitalOutput buzzer;
    GpioPinDigitalOutput servo;
    APA102 apa102;
    AlphanumericDisplay display;
    String displayMode = "None";

    private List<W1Device> oneWireDevices;
    private BMP280 bmp280; // temperarture and barometric pressure
    private BME280 bme280; // temperature, barometric pressure, and relative humidity
    private TSL2561 tsl2561; // visible and infrared light sensor
    private VL53L0X vl53l0x; // distance sensor based on time of flight
    private VCNL4010 vcnl4010; // luminance and proximity
    private LIS3DH lis3dh; // three-axis acceleration
    private HCSR04 hcsr04; // ultrasonic sensor
    private MPU6050 mpu6050; // gyroscope and accelerometer

    private DatabaseReference database;

    int sensorDataCollectionInterval = 1000; // milliseconds
    private long timeZeroMillis;
    private double currentTime;
    private double temperature;
    private double barometricPressure;
    private double relativeHumidity;
    private double visibleLux;
    private double infraredLux;
    private int lidarDistance;
    private float ultrasonicDistance;
    private int ax, ay, az;
    private double pitch, roll;
    private double[] accelerations;
    private double[] gyroAngularSpeeds;
    boolean allowTemperatureTransmission;
    boolean allowBarometricPressureTransmission;
    boolean allowRelativeHumidityTransmission;
    boolean allowVisibleLuxTransmission;
    boolean allowInfraredLuxTransmission;
    boolean allowDistanceTransmission;
    private String alphanumericString = "----";

    User user;
    TaskFactory taskFactory;
    BoardView boardView;
    WorkbenchGui gui;
    private ThreadPoolExecutor threadPool;
    private List<ThreadPoolListener> threadPoolListeners;

    private List<SensorDataPoint> temperatureDataStore;
    private List<SensorDataPoint>[] temperatureArrayDataStore;
    private List<SensorDataPoint> barometricPressureDataStore;
    private List<SensorDataPoint> relativeHumidityDataStore;
    private List<SensorDataPoint> visibleLuxDataStore;
    private List<SensorDataPoint> infraredLuxDataStore;
    private List<SensorDataPoint> lidarDistanceDataStore;
    private List<SensorDataPoint> ultrasonicDistanceDataStore;
    private List<SensorDataPoint> axDataStore;
    private List<SensorDataPoint> ayDataStore;
    private List<SensorDataPoint> azDataStore;
    private List<SensorDataPoint> pitchDataStore;
    private List<SensorDataPoint> rollDataStore;

    public IoTWorkbench() {
        init();
    }

    private void init() {

        user = new User();

        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        threadPoolListeners = new ArrayList<>();

        synchronizeWithCloud();

        Gpio.wiringPiSetup(); // initialize the wiringPi library, this is needed for PWM
        gpio = GpioFactory.getInstance();

        pirMotionSensor = gpio.provisionDigitalInputPin(RaspiPin.GPIO_06, "PIR Motion Sensor");
        pirMotionSensor.addTrigger(new GpioCallbackTrigger(PinState.HIGH, () -> {
            redLed.high();
            greenLed.high();
            blueLed.high();
            buzz(2);
            return null;
        }));
        pirMotionSensor.addTrigger(new GpioCallbackTrigger(PinState.LOW, () -> {
            redLed.low();
            greenLed.low();
            blueLed.low();
            buzz(0);
            return null;
        }));

        buttonA = gpio.provisionDigitalInputPin(RaspiPin.GPIO_29, "Button A");
        buttonB = gpio.provisionDigitalInputPin(RaspiPin.GPIO_28, "Button B");
        buttonC = gpio.provisionDigitalInputPin(RaspiPin.GPIO_27, "Button C");
        switch (boardType) {
            case RAINBOW_HAT:
                buzzer = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23, "Buzzer", PinState.LOW);
                redLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_22, "Red LED", PinState.LOW);
                greenLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24, "Green LED", PinState.LOW);
                blueLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "Blue LED", PinState.LOW);
                try {
                    display = new AlphanumericDisplay(AlphanumericDisplay.HT16K33.BLINK_OFF, AlphanumericDisplay.HT16K33.DUTY_01);
                    display.displayOn();
                    display.display(alphanumericString);
                } catch (Exception e) {
                    display = null;
                    e.printStackTrace();
                }
                try {
                    bmp280 = new BMP280(BMP280.Protocol.I2C, BMP280.ADDR_SDO_2_VDDIO, I2CBus.BUS_1);
                    bmp280.setIndoorNavigationMode();
                    bmp280.setMode(BMP280.Mode.NORMAL, true);
                    bmp280.setTemperatureSampleRate(BMP280.Temperature_Sample_Resolution.TWO, true);
                    bmp280.setPressureSampleRate(BMP280.Pressure_Sample_Resolution.SIXTEEN, true);
                    bmp280.setIIRFilter(BMP280.IIRFilter.SIXTEEN, true);
                    bmp280.setStandbyTime(BMP280.Standby_Time.MS_POINT_5, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    bmp280 = null;
                }
                break;
            case SENSOR_HUB:
                buzzer = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "Buzzer", PinState.LOW);
                servo = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "Servo", PinState.LOW);
                redLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_23, "Red LED", PinState.LOW);
                greenLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24, "Green LED", PinState.LOW);
                blueLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "Blue LED", PinState.LOW);
                W1Master master = new W1Master();
                oneWireDevices = master.getDevices(TmpDS18B20DeviceType.FAMILY_CODE);
                try { // temperature, pressure, and humidity
                    bme280 = new BME280();
                } catch (Exception e) {
                    e.printStackTrace();
                    bme280 = null;
                }
                try { // visible and infrared light
                    tsl2561 = new TSL2561();
                } catch (Exception e) {
                    e.printStackTrace();
                    tsl2561 = null;
                }
                try { // distance
                    vl53l0x = new VL53L0X();
                } catch (Exception e) {
                    e.printStackTrace();
                    vl53l0x = null;
                }
                try { // luminance and proximity
                    vcnl4010 = new VCNL4010();
                } catch (Exception e) {
                    e.printStackTrace();
                    vcnl4010 = null;
                }
                try { // three-axis acceleration
                    lis3dh = new LIS3DH();
                } catch (Exception e) {
                    e.printStackTrace();
                    lis3dh = null;
                }
                try {
                    mpu6050 = new MPU6050();
                } catch (Exception e) {
                    e.printStackTrace();
                    mpu6050 = null;
                }
                hcsr04 = new HCSR04();
                break;
        }

        final Preferences pref = Preferences.userNodeForPackage(IoTWorkbench.class);
        boardType = (byte) pref.getInt("board_type", RAINBOW_HAT);
        sensorDataCollectionInterval = pref.getInt("sensor_data_collection_interval", 1000);

        apa102 = new APA102(pref.getInt("number_of_rgb_leds", WorkbenchState.NUMBER_OF_RGB_LEDS));
        //apa102.setDefaultRainbow(); // test

        setupButtons();

        temperatureDataStore = Collections.synchronizedList(new ArrayList<>());
        barometricPressureDataStore = Collections.synchronizedList(new ArrayList<>());
        relativeHumidityDataStore = Collections.synchronizedList(new ArrayList<>());
        visibleLuxDataStore = Collections.synchronizedList(new ArrayList<>());
        infraredLuxDataStore = Collections.synchronizedList(new ArrayList<>());
        lidarDistanceDataStore = Collections.synchronizedList(new ArrayList<>());
        ultrasonicDistanceDataStore = Collections.synchronizedList(new ArrayList<>());
        axDataStore = Collections.synchronizedList(new ArrayList<>());
        ayDataStore = Collections.synchronizedList(new ArrayList<>());
        azDataStore = Collections.synchronizedList(new ArrayList<>());
        pitchDataStore = Collections.synchronizedList(new ArrayList<>());
        rollDataStore = Collections.synchronizedList(new ArrayList<>());

        startSensorDataCollection();

        taskFactory = new TaskFactory(this);

    }

    public byte getBoardType() {
        return boardType;
    }

    public void setBoardType(byte boardType) {
        this.boardType = boardType;
        if (boardView != null) {
            boardView.setBoardType(boardType);
        }
    }

    void clearDataStores() {
        temperatureDataStore.clear();
        barometricPressureDataStore.clear();
        relativeHumidityDataStore.clear();
        visibleLuxDataStore.clear();
        infraredLuxDataStore.clear();
        lidarDistanceDataStore.clear();
        ultrasonicDistanceDataStore.clear();
        axDataStore.clear();
        ayDataStore.clear();
        azDataStore.clear();
        pitchDataStore.clear();
        rollDataStore.clear();
        if (temperatureArrayDataStore != null) {
            for (List<SensorDataPoint> s : temperatureArrayDataStore) {
                s.clear();
            }
        }
        timeZeroMillis = System.currentTimeMillis();
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
        final Preferences pref = Preferences.userNodeForPackage(IoTWorkbench.class);
        pref.putInt("number_of_rgb_leds", numberOfRgbLeds);
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

    void rotateServo(int k) {
        if (servo != null) {
            int a = servo.getPin().getAddress();
            SoftPwm.softPwmStop(a);
            SoftPwm.softPwmCreate(a, 0, 50);
            switch (k) {
                case 1:
                    SoftPwm.softPwmWrite(a, 5);
                    break;
                case 2:
                    SoftPwm.softPwmWrite(a, 25);
                    break;
                case 3:
                    SoftPwm.softPwmWrite(a, 15);
                    break;
                default:
                    SoftPwm.softPwmWrite(a, 0);
            }
        }
    }

    void buzz(int k) {
        if (buzzer != null) {
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
        database.child("buttonA").setValue(on, null);
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
        database.child("buttonB").setValue(on, null);
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
        database.child("buttonC").setValue(on, null);
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
        threadPool.execute(() -> {
            while (true) {
                try {
                    currentTime = (double) (System.currentTimeMillis() - timeZeroMillis) / 1000.0;
                    if (bmp280 != null) {
                        double[] results = bmp280.sampleDeviceReads();
                        temperature = results[BMP280.TEMP_VAL_C];
                        barometricPressure = results[BMP280.PRES_VAL];
                        System.out.printf("BMP280: Time : %.2f s %n", currentTime);
                        System.out.printf("BMP280: Temperature in Celsius : %.2f C %n", temperature);
                        System.out.printf("BMP280: Pressure : %.2f hPa %n", barometricPressure);
                        temperatureDataStore.add(new SensorDataPoint(currentTime, temperature));
                        barometricPressureDataStore.add(new SensorDataPoint(currentTime, barometricPressure));
                        updateDisplay();
                        if (allowTemperatureTransmission) {
                            database.child("temperature").setValue(temperature, null);
                        }
                        if (allowBarometricPressureTransmission) {
                            database.child("barometricPressure").setValue(barometricPressure, null);
                        }
                    }
                    if (bme280 != null) {
                        bme280.read();
                        bme280.printf();
                        temperature = bme280.getTemperature();
                        barometricPressure = bme280.getPressure();
                        relativeHumidity = bme280.getRelativeHumidity();
                        relativeHumidityDataStore.add(new SensorDataPoint(currentTime, relativeHumidity));
                        temperatureDataStore.add(new SensorDataPoint(currentTime, temperature));
                        barometricPressureDataStore.add(new SensorDataPoint(currentTime, barometricPressure));
                        if (allowRelativeHumidityTransmission) {
                            database.child("relativeHumidity").setValue(relativeHumidity, null);
                        }
                        if (allowTemperatureTransmission) {
                            database.child("temperature").setValue(temperature, null);
                        }
                        if (allowBarometricPressureTransmission) {
                            database.child("barometricPressure").setValue(barometricPressure, null);
                        }
                    }
                    if (tsl2561 != null) {
                        tsl2561.read();
                        tsl2561.printf();
                        visibleLux = tsl2561.getVisibleLux();
                        infraredLux = tsl2561.getInfraredLux();
                        visibleLuxDataStore.add(new SensorDataPoint(currentTime, visibleLux));
                        infraredLuxDataStore.add(new SensorDataPoint(currentTime, infraredLux));
                        if (allowVisibleLuxTransmission) {
                            database.child("visibleLux").setValue(visibleLux, null);
                        }
                        if (allowInfraredLuxTransmission) {
                            database.child("infraredLux").setValue(infraredLux, null);
                        }
                    }
                    if (vl53l0x != null) {
                        lidarDistance = vl53l0x.range();
                        System.out.printf("VL53L0X: Distance : %d mm %n", lidarDistance);
                        lidarDistanceDataStore.add(new SensorDataPoint(currentTime, lidarDistance));
                        if (allowDistanceTransmission) {
                            database.child("distance").setValue(lidarDistance, null);
                        }
                    }
                    if (vcnl4010 != null) {
                        vcnl4010.read();
                        vcnl4010.printf();
                    }
                    if (lis3dh != null) {
                        lis3dh.read();
                        lis3dh.printf();
                        ax = lis3dh.getAx();
                        ay = lis3dh.getAy();
                        az = lis3dh.getAz();
                        pitch = lis3dh.getPitch();
                        roll = lis3dh.getRoll();
                        axDataStore.add(new SensorDataPoint(currentTime, ax));
                        ayDataStore.add(new SensorDataPoint(currentTime, ay));
                        azDataStore.add(new SensorDataPoint(currentTime, az));
                        pitchDataStore.add(new SensorDataPoint(currentTime, pitch));
                        rollDataStore.add(new SensorDataPoint(currentTime, roll));
                    }
                    if (mpu6050 != null) {
                        mpu6050.updateValues();
                        accelerations = mpu6050.getAccelAccelerations();
                        gyroAngularSpeeds = mpu6050.getGyroAngularSpeeds();
                        System.out.printf("MPU6050: Accelerations : %.2f, %.2f, %.2f %n", accelerations[0], accelerations[1], accelerations[2]);
                        System.out.printf("MPU6050: Gyro Angular Speeds : %.2f, %.2f, %.2f %n", gyroAngularSpeeds[0], gyroAngularSpeeds[1], gyroAngularSpeeds[2]);
                    }
                    if (hcsr04 != null) {
                        float x = hcsr04.getDistance();
                        if (!Float.isNaN(x)) {
                            ultrasonicDistance = x;
                            System.out.printf("HC-SR04: Distance : %.3f mm %n", ultrasonicDistance);
                            ultrasonicDistanceDataStore.add(new SensorDataPoint(currentTime, ultrasonicDistance));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (boardView != null) {
                    boardView.repaint();
                }
                try {
                    Thread.sleep(sensorDataCollectionInterval);
                } catch (InterruptedException e) {
                }
            }
        });
        startOneWireTemperatureSensorDataCollection();
    }

    // one-wire temperature sensors are much slower, so handle them in a separate thread
    private void startOneWireTemperatureSensorDataCollection() {
        if (oneWireDevices == null || oneWireDevices.isEmpty()) {
            return;
        }
        temperatureArrayDataStore = new List[oneWireDevices.size()];
        for (int i = 0; i < temperatureArrayDataStore.length; i++) {
            temperatureArrayDataStore[i] = Collections.synchronizedList(new ArrayList<>());
        }
        threadPool.execute(() -> {
            while (true) {
                for (int i = 0; i < oneWireDevices.size(); i++) {
                    W1Device device = oneWireDevices.get(i);
                    if (device instanceof TemperatureSensor) {
                        double tmp = ((TemperatureSensor) device).getTemperature();
                        currentTime = (double) (System.currentTimeMillis() - timeZeroMillis) / 1000.0;
                        temperatureArrayDataStore[i].add(new SensorDataPoint(currentTime, tmp));
                        System.out.printf("DS18B20: Temperature (%s) : %.2f C %n", device.getId(), tmp);
                    }
                }
                if (boardView != null) {
                    boardView.repaint();
                }
//                try {
//                    Thread.sleep(sensorDataCollectionInterval);
//                } catch (InterruptedException e) {
//                }
            }
        });
    }

    public void setSensorDataCollectionInterval(int sensorDataCollectionInterval) {
        this.sensorDataCollectionInterval = sensorDataCollectionInterval;
        final Preferences pref = Preferences.userNodeForPackage(IoTWorkbench.class);
        pref.putInt("sensor_data_collection_interval", sensorDataCollectionInterval);
        database.child("sensorDataCollectionInterval").setValue(sensorDataCollectionInterval, null);
    }

    public int getSensorDataCollectionInterval() {
        return sensorDataCollectionInterval;
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

    public List<SensorDataPoint>[] getTemperatureArrayDataStore() {
        return temperatureArrayDataStore;
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

    public void setAllowRelativeHumidityTransmission(boolean b) {
        allowRelativeHumidityTransmission = b;
        database.child("allowRelativeHumidityTransmission").setValue(b, null);
    }

    public boolean getAllowRelativeHumidityTransmission() {
        return allowRelativeHumidityTransmission;
    }

    public double getRelativeHumidity() {
        return relativeHumidity;
    }

    public List<SensorDataPoint> getRelativeHumidityDataStore() {
        return relativeHumidityDataStore;
    }

    public void setAllowVisibleLuxTransmission(boolean b) {
        allowVisibleLuxTransmission = b;
        database.child("allowVisibleLuxTransmission").setValue(b, null);
    }

    public boolean getAllowVisibleLuxTransmission() {
        return allowVisibleLuxTransmission;
    }

    public double getVisibleLux() {
        return visibleLux;
    }

    public List<SensorDataPoint> getVisibleLuxDataStore() {
        return visibleLuxDataStore;
    }

    public void setAllowInfraredLuxTransmission(boolean b) {
        allowInfraredLuxTransmission = b;
        database.child("allowInfraredLuxTransmission").setValue(b, null);
    }

    public boolean getAllowInfraredLuxTransmission() {
        return allowInfraredLuxTransmission;
    }

    public double getInfraredLux() {
        return infraredLux;
    }

    public List<SensorDataPoint> getInfraredLuxDataStore() {
        return infraredLuxDataStore;
    }

    public void setAllowDistanceTransmission(boolean b) {
        allowDistanceTransmission = b;
        database.child("allowDistanceTransmission").setValue(b, null);
    }

    public boolean getAllowDistanceTransmission() {
        return allowDistanceTransmission;
    }

    public int getLidarDistance() {
        return lidarDistance;
    }

    public List<SensorDataPoint> getLidarDistanceDataStore() {
        return lidarDistanceDataStore;
    }

    public float getUltrasonicDistance() {
        return ultrasonicDistance;
    }

    public List<SensorDataPoint> getUltrasonicDistanceDataStore() {
        return ultrasonicDistanceDataStore;
    }

    public int getAx() {
        return ax;
    }

    public List<SensorDataPoint> getAxDataStore() {
        return axDataStore;
    }

    public int getAy() {
        return ay;
    }

    public List<SensorDataPoint> getAyDataStore() {
        return ayDataStore;
    }

    public int getAz() {
        return az;
    }

    public List<SensorDataPoint> getAzDataStore() {
        return azDataStore;
    }

    public double getPitch() {
        return pitch;
    }

    public List<SensorDataPoint> getPitchDataStore() {
        return pitchDataStore;
    }

    public double getRoll() {
        return roll;
    }

    public List<SensorDataPoint> getRollDataStore() {
        return rollDataStore;
    }

    private void synchronizeWithCloud() {
        try {
            FileInputStream serviceAccount = new FileInputStream("raspberry-pi-java-firebase-adminsdk-eeeo1-f7e5dc2054.json");
            FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).setDatabaseUrl("https://raspberry-pi-java.firebaseio.com").build();
            FirebaseApp.initializeApp(options);
            String reference = "iot_workbench";
            switch (boardType) {
                case RAINBOW_HAT:
                    reference = "rainbow_hat";
                    break;
                case SENSOR_HUB:
                    reference = "sensor_hub";
                    break;
            }
            database = FirebaseDatabase.getInstance().getReference(reference + "_" + user.getName());
            // database.setValue(new WorkbenchState(), null);
            database.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) { // This method is called once with the initial value and again whenever data at this location is updated.
                    DatabaseHandler.handle(IoTWorkbench.this, dataSnapshot);
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
        pirMotionSensor.removeAllTriggers();
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
        boardView = new BoardView(this);
        gui = new WorkbenchGui();
        gui.createAndShowGui(this);
        boardView.addGraphListener(gui);
        final Preferences pref = Preferences.userNodeForPackage(IoTWorkbench.class);
        boardView.setShowGraph(pref.getBoolean("show_graph", false));
    }

    void setLedColorsOnBoardView() {
        if (boardView != null) {
            for (int i = 0; i < boardView.getActualNumberOfRgbLeds(); i++) {
                boardView.setLedColor(i, apa102.getColor(i));
            }
        }
    }

    void setDefaultRainbow() {
        apa102.setDefaultRainbow();
        setLedColorsOnBoardView();
        ArrayList<ArrayList<Integer>> list = new ArrayList<>();
        for (int i = 0; i < WorkbenchState.NUMBER_OF_RGB_LEDS; i++) { // only save the state of the seven LEDs on the HAT
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
            for (int j = 0; j < WorkbenchState.NUMBER_OF_RGB_LEDS; j++) {
                list.add(i == j ? getRgb(c) : getRgb(apa102.getColor(j)));
            }
            database.child("rainbowRgb").setValue(list, null); // TODO: There must be a way to set only the element of the list
        }
    }

    public static void main(final String[] args) {

        final IoTWorkbench ioTWorkbench = new IoTWorkbench();

        if (GraphicsEnvironment.isHeadless()) {

            Scanner scanner = new Scanner(System.in);
            String line = "";
            while (!"q".equalsIgnoreCase(line)) {
                line = scanner.next();
                System.out.println("You typed: " + line);
            }
            scanner.close();
            ioTWorkbench.destroy();
            System.exit(0); // call this to exit and avoid a broken pipe error

        } else {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            EventQueue.invokeLater(() -> ioTWorkbench.createAndShowGui());
        }

    }

}