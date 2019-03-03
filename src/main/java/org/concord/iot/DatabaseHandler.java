package org.concord.iot;

import com.google.firebase.database.DataSnapshot;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author Charles Xie
 */

class DatabaseHandler {

    static void handle(RainbowHat rainbowHat, DataSnapshot dataSnapshot) {

        System.out.println("Value changed: " + dataSnapshot.getValue());

        RainbowHatState state = dataSnapshot.getValue(RainbowHatState.class);
        rainbowHat.allowTemperatureTransmission = state.allowTemperatureTransmission;
        rainbowHat.allowBarometricPressureTransmission = state.allowBarometricPressureTransmission;
        if (rainbowHat.gui != null) {
            rainbowHat.gui.setUploadTemperatureCheckBox(rainbowHat.allowTemperatureTransmission);
            rainbowHat.gui.setUploadPressureCheckBox(rainbowHat.allowBarometricPressureTransmission);
        }

        if (state.redLed != rainbowHat.redLed.isHigh()) { // change only when the remote state is not the same as the local state
            rainbowHat.setRedLedState(state.redLed, false);
            if (state.redLed) {
                rainbowHat.buzz(1);
            }
        }
        if (state.greenLed != rainbowHat.greenLed.isHigh()) {
            rainbowHat.setGreenLedState(state.greenLed, false);
            if (state.greenLed) {
                rainbowHat.buzz(2);
            }
        }
        if (state.blueLed != rainbowHat.blueLed.isHigh()) {
            rainbowHat.setBlueLedState(state.blueLed, false);
            if (state.blueLed) {
                rainbowHat.buzz(3);
            }
        }

        for (int i = 0; i < RainbowHatState.NUMBER_OF_RGB_LEDS; i++) { // the led strip goes from the right to the left (0 is the rightmost and 6 is the leftmost).
            ArrayList<Integer> rgb = state.rainbowRgb.get(i);
            Color c = new Color(rgb.get(0), rgb.get(1), rgb.get(2));
            rainbowHat.apa102.setColor(i, c);
            if (rainbowHat.boardView != null) {
                rainbowHat.boardView.setLedColor(i, c);
            }
        }

        rainbowHat.displayMode = state.displayMode;

        boolean testNow = false;
        if (testNow) {
            rainbowHat.taskFactory.stopAllTasks();
            if ("blink_all_leds".equals(state.task)) {
                rainbowHat.taskFactory.blinkApaTask.setStopped(false);
                rainbowHat.taskFactory.blinkApaTask.run();
            } else if ("move_rainbow".equals(state.task)) {
                rainbowHat.taskFactory.movingRainbowApaTask.setStopped(false);
                rainbowHat.taskFactory.movingRainbowApaTask.run();
            } else if ("move_trains".equals(state.task)) {
                rainbowHat.taskFactory.movingTrainsApaTask.setStopped(false);
                rainbowHat.taskFactory.movingTrainsApaTask.run();
            } else if ("bounce_dot".equals(state.task)) {
                rainbowHat.taskFactory.bouncingDotApaTask.setStopped(false);
                rainbowHat.taskFactory.bouncingDotApaTask.run();
            } else if ("ripple_effect".equals(state.task)) {
                rainbowHat.taskFactory.rippleEffectApaTask.setStopped(false);
                rainbowHat.taskFactory.rippleEffectApaTask.run();
            } else if ("random_colors".equals(state.task)) {
                rainbowHat.taskFactory.randomColorsApaTask.setStopped(false);
                rainbowHat.taskFactory.randomColorsApaTask.run();
            }
        }

    }

}