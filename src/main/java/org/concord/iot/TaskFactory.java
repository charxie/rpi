package org.concord.iot;

import java.awt.*;
import java.util.Arrays;

/**
 * @author Charles Xie
 */

class TaskFactory {

    private IoTWorkbench workbench;
    private final Object lock = new Object();

    Task rotateServoTask;
    Task blinkRedLedTask;
    Task blinkGreenLedTask;
    Task blinkBlueLedTask;
    Task jumpLedTask;
    Task randomColorsApaTask;
    Task blinkApaTask;
    Task movingRainbowApaTask;
    Task bouncingDotApaTask;
    Task movingTrainsApaTask;
    Task rippleEffectApaTask;

    TaskFactory(IoTWorkbench workbench) {
        this.workbench = workbench;
        createTasks();
    }

    Object getLock() {
        return lock;
    }

    void stopAllApaTasks() {
        randomColorsApaTask.setStopped(true);
        blinkApaTask.setStopped(true);
        movingRainbowApaTask.setStopped(true);
        bouncingDotApaTask.setStopped(true);
        movingTrainsApaTask.setStopped(true);
        rippleEffectApaTask.setStopped(true);
    }

    private void createTasks() {

        rotateServoTask = new Task("Rotate Servo", workbench);
        rotateServoTask.setRunnable(() -> {
            while (true) {
                try {
                    workbench.rotateServo(1);
                    Thread.sleep(500);
                    workbench.rotateServo(2);
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                if (rotateServoTask.isStopped()) {
                    workbench.rotateServo(0);
                    break;
                }
            }
        });

        blinkRedLedTask = new Task("Blinking Red LED", workbench);
        blinkRedLedTask.setRunnable(() -> {
            while (true) {
                try {
                    workbench.setRedLedState(true, true);
                    Thread.sleep(500);
                    workbench.setRedLedState(false, true);
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                if (blinkRedLedTask.isStopped()) {
                    break;
                }
            }
        });

        blinkGreenLedTask = new Task("Blinking Green LED", workbench);
        blinkGreenLedTask.setRunnable(() -> {
            while (true) {
                try {
                    workbench.setGreenLedState(true, true);
                    Thread.sleep(500);
                    workbench.setGreenLedState(false, true);
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                if (blinkGreenLedTask.isStopped()) {
                    break;
                }
            }
        });

        blinkBlueLedTask = new Task("Blinking Blue LED", workbench);
        blinkBlueLedTask.setRunnable(() -> {
            while (true) {
                try {
                    workbench.setBlueLedState(true, true);
                    Thread.sleep(500);
                    workbench.setBlueLedState(false, true);
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                if (blinkBlueLedTask.isStopped()) {
                    break;
                }
            }
        });

        jumpLedTask = new Task("Jumping LEDs", workbench);
        jumpLedTask.setRunnable(() -> {
            while (true) {
                try {
                    workbench.setRedLedState(true, true);
                    Thread.sleep(500);
                    workbench.setRedLedState(false, true);
                    workbench.setGreenLedState(true, true);
                    Thread.sleep(500);
                    workbench.setGreenLedState(false, true);
                    workbench.setBlueLedState(true, true);
                    Thread.sleep(500);
                    workbench.setBlueLedState(false, true);
                } catch (InterruptedException ex) {
                }
                if (jumpLedTask.isStopped()) {
                    break;
                }
            }
        });

        blinkApaTask = new Task("Blink All", workbench);
        blinkApaTask.setRunnable(() -> {
            while (true) {
                Color c = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
                try {
                    workbench.apa102.setColorForAll(c);
                    if (workbench.boardView != null) {
                        workbench.boardView.setColorForAllLeds(c);
                    }
                    Thread.sleep(500);
                    workbench.apa102.setColorForAll(Color.BLACK);
                    if (workbench.boardView != null) {
                        workbench.boardView.setColorForAllLeds(Color.BLACK);
                    }
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (blinkApaTask.isStopped()) {
                    break;
                }
            }
        });

        movingRainbowApaTask = new Task("Moving Rainbows", workbench);
        movingRainbowApaTask.setRunnable(() -> {
            workbench.apa102.resetShift();
            while (true) {
                synchronized (lock) {
                    workbench.apa102.moveRainbow(1);
                    workbench.apa102.shift(0.01f);
                    workbench.setLedColorsOnBoardView();
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                if (movingRainbowApaTask.isStopped()) {
                    break;
                }
            }
        });

        randomColorsApaTask = new Task("Random Colors", workbench);
        randomColorsApaTask.setRunnable(() -> {
            byte[][] data = new byte[workbench.getNumberOfRgbLeds()][4];
            while (true) {
                synchronized (lock) {
                    for (int i = 0; i < data.length; i++) {
                        data[i][0] = (byte) (255 * Math.random());
                        data[i][1] = (byte) (255 * Math.random());
                        data[i][2] = (byte) (255 * Math.random());
                    }
                    workbench.apa102.setData(data);
                    workbench.setLedColorsOnBoardView();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                if (randomColorsApaTask.isStopped()) {
                    break;
                }
            }
        });

        bouncingDotApaTask = new Task("Bouncing Dot", workbench);
        bouncingDotApaTask.setRunnable(() -> {
            byte[][] data = new byte[workbench.getNumberOfRgbLeds()][4];
            bouncingDotApaTask.setIndex(0);
            boolean reverse = false;
            while (true) {
                synchronized (lock) {
                    int index = bouncingDotApaTask.getIndex();
                    for (int i = 0; i < data.length; i++) {
                        if (i == index) {
                            data[index][0] = (byte) 255;
                            data[index][1] = (byte) 0;
                            data[index][2] = (byte) 0;
                        } else {
                            Arrays.fill(data[i], (byte) 0);
                        }
                    }
                    workbench.apa102.setData(data);
                    workbench.setLedColorsOnBoardView();
                }
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                }
                workbench.buzz(0);
                if (reverse) {
                    bouncingDotApaTask.addToIndex(-1);
                    if (bouncingDotApaTask.getIndex() < 0) {
                        bouncingDotApaTask.setIndex(0);
                        reverse = false;
                        workbench.buzz(1);
                    }
                } else {
                    bouncingDotApaTask.addToIndex(1);
                    if (bouncingDotApaTask.getIndex() >= data.length) {
                        bouncingDotApaTask.setIndex(data.length - 1);
                        reverse = true;
                        workbench.buzz(1);
                    }
                }
                if (bouncingDotApaTask.isStopped()) {
                    break;
                }
            }
        });

        movingTrainsApaTask = new Task("Moving Trains", workbench);
        movingTrainsApaTask.setRunnable(() -> {
            byte[][] data = new byte[workbench.getNumberOfRgbLeds()][4];
            int trainLength = 7;
            int interval = 10;
            int m = 20;
            Color c = Color.RED;
            movingTrainsApaTask.setIndex(0);
            while (true) {
                synchronized (lock) {
                    int firstIndexOfTrain = movingTrainsApaTask.getIndex();
                    int lastIndexOfTrain = firstIndexOfTrain - trainLength;
                    boolean onTrain;
                    int max = Math.round((float) (m * data.length) / (float) (trainLength + interval));
                    for (int i = 0; i < data.length; i++) {
                        onTrain = false;
                        Arrays.fill(data[i], (byte) 0);
                        for (int k = 0; k < max; k++) {
                            if (i <= firstIndexOfTrain - (trainLength + interval) * k && i > lastIndexOfTrain - (trainLength + interval) * k) {
                                onTrain = true;
                                break;
                            }
                        }
                        if (onTrain) {
                            data[i][0] = (byte) c.getRed();
                            data[i][1] = (byte) c.getGreen();
                            data[i][2] = (byte) c.getBlue();
                        }
                    }
                    workbench.apa102.setData(data);
                    workbench.setLedColorsOnBoardView();
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                }
                movingTrainsApaTask.addToIndex(1);
                if (movingTrainsApaTask.getIndex() >= m * data.length + trainLength) {
                    movingTrainsApaTask.setIndex(0);
                    c = new Color((int) (255 * Math.random()), (int) (255 * Math.random()), (int) (255 * Math.random()));
                }
                if (movingTrainsApaTask.isStopped()) {
                    break;
                }
            }
        });

        rippleEffectApaTask = new Task("Ripple Effect", workbench);
        rippleEffectApaTask.setRunnable(() -> {
            byte[][] data = new byte[workbench.getNumberOfRgbLeds()][4];
            double wavelength = 20;
            double speed = 1;
            rippleEffectApaTask.setIndex(0);
            while (true) {
                synchronized (lock) {
                    int time = rippleEffectApaTask.getIndex();
                    for (int i = 0; i < data.length; i++) {
                        data[i][0] = (byte) Math.min(255, 128 + 128 * Math.sin(2.0 * Math.PI * (i + (i < data.length / 2 ? speed : -speed) * time) / wavelength));
                        data[i][1] = (byte) 0;
                        data[i][2] = (byte) (255 - data[i][0]);
                    }
                    workbench.apa102.setData(data);
                    workbench.setLedColorsOnBoardView();
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ex) {
                }
                rippleEffectApaTask.addToIndex(1);
                if (rippleEffectApaTask.isStopped()) {
                    break;
                }
            }
        });

    }

}