package org.concord.iot;

import com.google.firebase.database.DataSnapshot;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author Charles Xie
 */

class DatabaseHandler {

    static void handle(IoTWorkbench workbench, DataSnapshot dataSnapshot) {

        System.out.println("Value changed : " + dataSnapshot.getValue());

        WorkbenchState state = null;
        try {
            state = dataSnapshot.getValue(WorkbenchState.class);
        } catch (Exception e) {
            // sometime I edited the Firebase that accidentially caused the state variable to be incompatible, this should catch the error
            e.printStackTrace();
        }
        workbench.allowTemperatureTransmission = state.allowTemperatureTransmission;
        workbench.allowBarometricPressureTransmission = state.allowBarometricPressureTransmission;
        workbench.allowRelativeHumidityTransmission = state.allowRelativeHumidityTransmission;
        workbench.allowVisibleLuxTransmission = state.allowVisibleLuxTransmission;
        workbench.allowInfraredLuxTransmission = state.allowInfraredLuxTransmission;
        workbench.allowDistanceTransmission = state.allowDistanceTransmission;

        if (state.redLed != workbench.redLed.isHigh()) { // change only when the remote state is not the same as the local state
            workbench.setRedLedState(state.redLed, false);
            if (state.redLed) {
                workbench.buzz(1);
            }
        }
        if (state.greenLed != workbench.greenLed.isHigh()) {
            workbench.setGreenLedState(state.greenLed, false);
            if (state.greenLed) {
                workbench.buzz(2);
            }
        }
        if (state.blueLed != workbench.blueLed.isHigh()) {
            workbench.setBlueLedState(state.blueLed, false);
            if (state.blueLed) {
                workbench.buzz(3);
            }
        }

        for (int i = 0; i < WorkbenchState.NUMBER_OF_RGB_LEDS; i++) { // the led strip goes from the right to the left (0 is the rightmost and 6 is the leftmost).
            ArrayList<Integer> rgb = state.rainbowRgb.get(i);
            Color c = new Color(rgb.get(0), rgb.get(1), rgb.get(2));
            workbench.apa102.setColor(i, c);
            if (workbench.boardView != null) {
                workbench.boardView.setLedColor(i, c);
            }
        }

        workbench.displayMode = state.displayMode;

        boolean testNow = false;
        if (testNow) {
            workbench.taskFactory.stopAllApaTasks();
            if ("blink_all_leds".equals(state.task)) {
                workbench.taskFactory.blinkApaTask.setStopped(false);
                workbench.taskFactory.blinkApaTask.run();
            } else if ("move_rainbow".equals(state.task)) {
                workbench.taskFactory.movingRainbowApaTask.setStopped(false);
                workbench.taskFactory.movingRainbowApaTask.run();
            } else if ("move_trains".equals(state.task)) {
                workbench.taskFactory.movingTrainsApaTask.setStopped(false);
                workbench.taskFactory.movingTrainsApaTask.run();
            } else if ("bounce_dot".equals(state.task)) {
                workbench.taskFactory.bouncingDotApaTask.setStopped(false);
                workbench.taskFactory.bouncingDotApaTask.run();
            } else if ("ripple_effect".equals(state.task)) {
                workbench.taskFactory.rippleEffectApaTask.setStopped(false);
                workbench.taskFactory.rippleEffectApaTask.run();
            } else if ("random_colors".equals(state.task)) {
                workbench.taskFactory.randomColorsApaTask.setStopped(false);
                workbench.taskFactory.randomColorsApaTask.run();
            }
        }

    }

}