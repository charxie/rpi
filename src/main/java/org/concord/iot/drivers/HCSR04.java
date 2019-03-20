package org.concord.iot.drivers;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

/**
 * For information, see https://components101.com/ultrasonic-sensor-working-pinout-datasheet
 *
 * @author Rutger Claes <rutger.claes@cs.kuleuven.be>
 * @author Charles Xie
 */

public class HCSR04 {

    private final static int TIMEOUT = 2100;
    private final static float SOUND_SPEED = 340.29f;  // speed of sound in m/s
    private final static int TRIG_DURATION_IN_MICROS = 10; // trigger duration of 10 micro s

    private final GpioPinDigitalInput echoPin;
    private final GpioPinDigitalOutput trigPin;

    public HCSR04() {
        GpioController gpio = GpioFactory.getInstance();
        echoPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04);
        trigPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05);
        trigPin.low();
    }

    // return measurement in mm
    public float getDistance() {
        float distance;
        try {
            triggerSensor();
            waitForSignal();
            distance = measureSignal() * SOUND_SPEED / 2000f;
        } catch (Exception e) {
            System.out.println("No target detected");
            distance = Float.NaN;
        }
        return distance;
    }

    // Set high on the trig pin for TRIG_DURATION_IN_MICROS
    private void triggerSensor() {
        try {
            trigPin.high();
            Thread.sleep(0, TRIG_DURATION_IN_MICROS * 1000);
            trigPin.low();
        } catch (InterruptedException ex) {
            System.err.println("Interrupt during trigger");
        }
    }

    // Wait for high on the echo pin
    private void waitForSignal() throws Exception {
        int countdown = TIMEOUT;
        while (echoPin.isLow() && countdown > 0) {
            countdown--;
        }
        if (countdown <= 0) {
            throw new Exception("Timeout waiting for signal start");
        }
    }

    /*
     * @return the duration of the signal in micro seconds
     * @throws Exception if no low appears in time
     */
    private long measureSignal() throws Exception {
        int countdown = TIMEOUT;
        long start = System.nanoTime();
        while (echoPin.isHigh() && countdown > 0) {
            countdown--;
        }
        long end = System.nanoTime();
        if (countdown <= 0) {
            throw new Exception("Timeout waiting for signal end");
        }
        return (long) Math.ceil((end - start) / 1000.0);  // Return micro seconds
    }

}