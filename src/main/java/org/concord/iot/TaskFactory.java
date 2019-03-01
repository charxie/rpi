package org.concord.iot;

import java.awt.*;
import java.util.Arrays;

/**
 * @author Charles Xie
 */

class TaskFactory {

    private RainbowHat rainbowHat;
    private final Object lock = new Object();

    Task blinkRedLedTask;
    Task blinkGreenLedTask;
    Task blinkBlueLedTask;
    Task jumpLedTask;
    Task randomColorApaTask;
    Task blinkApaTask;
    Task movingRainbowApaTask;
    Task bouncingDotApaTask;
    Task movingTrainsApaTask;
    Task rippleEffectApaTask;

    TaskFactory(RainbowHat rainbowHat) {
        this.rainbowHat = rainbowHat;
        createTasks();
    }

    Object getLock() {
        return lock;
    }

    private void createTasks() {

        blinkRedLedTask = new Task("Blinking Red LED", rainbowHat);
        blinkRedLedTask.setRunnable(() -> {
            while (true) {
                try {
                    rainbowHat.setRedLedState(true, true);
                    Thread.sleep(500);
                    rainbowHat.setRedLedState(false, true);
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                if (blinkRedLedTask.isStopped()) {
                    break;
                }
            }
        });

        blinkGreenLedTask = new Task("Blinking Green LED", rainbowHat);
        blinkGreenLedTask.setRunnable(() -> {
            while (true) {
                try {
                    rainbowHat.setGreenLedState(true, true);
                    Thread.sleep(500);
                    rainbowHat.setGreenLedState(false, true);
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                if (blinkGreenLedTask.isStopped()) {
                    break;
                }
            }
        });

        blinkBlueLedTask = new Task("Blinking Blue LED", rainbowHat);
        blinkBlueLedTask.setRunnable(() -> {
            while (true) {
                try {
                    rainbowHat.setBlueLedState(true, true);
                    Thread.sleep(500);
                    rainbowHat.setBlueLedState(false, true);
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                if (blinkBlueLedTask.isStopped()) {
                    break;
                }
            }
        });

        jumpLedTask = new Task("Jumping LEDs", rainbowHat);
        jumpLedTask.setRunnable(() -> {
            while (true) {
                try {
                    rainbowHat.setRedLedState(true, true);
                    Thread.sleep(500);
                    rainbowHat.setRedLedState(false, true);
                    rainbowHat.setGreenLedState(true, true);
                    Thread.sleep(500);
                    rainbowHat.setGreenLedState(false, true);
                    rainbowHat.setBlueLedState(true, true);
                    Thread.sleep(500);
                    rainbowHat.setBlueLedState(false, true);
                } catch (InterruptedException ex) {
                }
                if (jumpLedTask.isStopped()) {
                    break;
                }
            }
        });

        blinkApaTask = new Task("Blink All", rainbowHat);
        blinkApaTask.setRunnable(() -> {
            while (true) {
                Color c = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
                try {
                    rainbowHat.apa102.setColorForAll(c);
                    if (rainbowHat.boardView != null) {
                        rainbowHat.boardView.setColorForAllLeds(c);
                    }
                    Thread.sleep(500);
                    rainbowHat.apa102.setColorForAll(Color.BLACK);
                    if (rainbowHat.boardView != null) {
                        rainbowHat.boardView.setColorForAllLeds(Color.BLACK);
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

        movingRainbowApaTask = new Task("Moving Rainbows", rainbowHat);
        movingRainbowApaTask.setRunnable(() -> {
            rainbowHat.apa102.resetShift();
            while (true) {
                synchronized (lock) {
                    rainbowHat.apa102.moveRainbow(1);
                    rainbowHat.apa102.shift(0.01f);
                    rainbowHat.setLedColorsOnBoardView();
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

        randomColorApaTask = new Task("Random Colors", rainbowHat);
        randomColorApaTask.setRunnable(() -> {
            byte[][] data = new byte[rainbowHat.getNumberOfRgbLeds()][4];
            while (true) {
                synchronized (lock) {
                    for (int i = 0; i < data.length; i++) {
                        data[i][0] = (byte) (255 * Math.random());
                        data[i][1] = (byte) (255 * Math.random());
                        data[i][2] = (byte) (255 * Math.random());
                    }
                    rainbowHat.apa102.setData(data);
                    rainbowHat.setLedColorsOnBoardView();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                if (randomColorApaTask.isStopped()) {
                    break;
                }
            }
        });

        bouncingDotApaTask = new Task("Bouncing Dot", rainbowHat);
        bouncingDotApaTask.setRunnable(() -> {
            byte[][] data = new byte[rainbowHat.getNumberOfRgbLeds()][4];
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
                    rainbowHat.apa102.setData(data);
                    rainbowHat.setLedColorsOnBoardView();
                }
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                }
                rainbowHat.buzz(0);
                if (reverse) {
                    bouncingDotApaTask.addToIndex(-1);
                    if (bouncingDotApaTask.getIndex() < 0) {
                        bouncingDotApaTask.setIndex(0);
                        reverse = false;
                        rainbowHat.buzz(1);
                    }
                } else {
                    bouncingDotApaTask.addToIndex(1);
                    if (bouncingDotApaTask.getIndex() >= data.length) {
                        bouncingDotApaTask.setIndex(data.length - 1);
                        reverse = true;
                        rainbowHat.buzz(1);
                    }
                }
                if (bouncingDotApaTask.isStopped()) {
                    break;
                }
            }
        });

        movingTrainsApaTask = new Task("Moving Trains", rainbowHat);
        movingTrainsApaTask.setRunnable(() -> {
            byte[][] data = new byte[rainbowHat.getNumberOfRgbLeds()][4];
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
                    rainbowHat.apa102.setData(data);
                    rainbowHat.setLedColorsOnBoardView();
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

        rippleEffectApaTask = new Task("Ripple Effect", rainbowHat);
        rippleEffectApaTask.setRunnable(() -> {
            byte[][] data = new byte[rainbowHat.getNumberOfRgbLeds()][4];
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
                    rainbowHat.apa102.setData(data);
                    rainbowHat.setLedColorsOnBoardView();
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