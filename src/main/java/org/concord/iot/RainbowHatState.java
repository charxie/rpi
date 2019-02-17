package org.concord.iot;

import java.util.ArrayList;

/**
 * @author Charles Xie
 */

public class RainbowHatState {

    public final static int NUMBER_OF_LED_IN_STRIP = 7;

    public boolean redLed;
    public boolean greenLed;
    public boolean blueLed;
    public boolean buzzer;
    public ArrayList<ArrayList<Integer>> ledStripColors = new ArrayList<>(NUMBER_OF_LED_IN_STRIP);

    public float temperature = 20; // Celsius
    public float barometricPressure = 1000; // hPa
    public boolean allowTemperatureTransmission;
    public boolean allowBarometricPressureTransmission;

    public String displayMode = "Temperature";

    public RainbowHatState() {
        for (int i = 0; i < NUMBER_OF_LED_IN_STRIP; i++) {
            ArrayList<Integer> rgb = new ArrayList<>(3);
            rgb.add(0);
            rgb.add(0);
            rgb.add(0);
            ledStripColors.add(rgb);
        }
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

    public ArrayList<ArrayList<Integer>> getLedStripColors() {
        return ledStripColors;
    }

    public void setLedStripColors(ArrayList<ArrayList<Integer>> ledStripColors) {
        this.ledStripColors = ledStripColors;
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