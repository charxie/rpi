package org.concord.iot;

import java.util.ArrayList;

/**
 * @author Charles Xie
 */

public class RainbowHatState {

    public final static int NUMBER_OF_RGB_LEDS = 7;

    public String task;
    public boolean redLed;
    public boolean greenLed;
    public boolean blueLed;
    public boolean buzzer;
    public ArrayList<ArrayList<Integer>> rainbowRgb = new ArrayList<>(NUMBER_OF_RGB_LEDS);

    public float temperature = 20; // Celsius
    public float barometricPressure = 1000; // hPa
    public boolean allowTemperatureTransmission;
    public boolean allowBarometricPressureTransmission;

    public String displayMode = "Temperature";

    public RainbowHatState() {
        for (int i = 0; i < NUMBER_OF_RGB_LEDS; i++) {
            ArrayList<Integer> rgb = new ArrayList<>(3);
            rgb.add(0);
            rgb.add(0);
            rgb.add(0);
            rainbowRgb.add(rgb);
        }
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public boolean getAllowTemperatureTransmission() {
        return allowTemperatureTransmission;
    }

    public void setAllowTemperatureTransmission(boolean allowTemperatureTransmission) {
        this.allowTemperatureTransmission = allowTemperatureTransmission;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public boolean getAllowBarometricPressureTransmission() {
        return allowBarometricPressureTransmission;
    }

    public void setAllowBarometricPressureTransmission(boolean allowBarometricPressureTransmission) {
        this.allowBarometricPressureTransmission = allowBarometricPressureTransmission;
    }

    public float getBarometricPressure() {
        return barometricPressure;
    }

    public void setBarometricPressure(float barometricPressure) {
        this.barometricPressure = barometricPressure;
    }

    public ArrayList<ArrayList<Integer>> getRainbowRgb() {
        return rainbowRgb;
    }

    public void setRainbowRgb(ArrayList<ArrayList<Integer>> rainbowRgb) {
        this.rainbowRgb = rainbowRgb;
    }

    public boolean getRedLed() {
        return redLed;
    }

    public void setRedLed(boolean redLed) {
        this.redLed = redLed;
    }

    public boolean getGreenLed() {
        return greenLed;
    }

    public void setGreenLed(boolean greenLed) {
        this.greenLed = greenLed;
    }

    public boolean getBlueLed() {
        return blueLed;
    }

    public void setBlueLed(boolean blueLed) {
        this.blueLed = blueLed;
    }

    public boolean getBuzzer() {
        return buzzer;
    }

    public void setBuzzer(boolean buzzer) {
        this.buzzer = buzzer;
    }

    public String getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(String displayMode) {
        this.displayMode = displayMode;
    }

    @Override
    public String toString() {
        return "Red LED: " + redLed;
    }

}