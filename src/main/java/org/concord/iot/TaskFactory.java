package org.concord.iot;

import java.awt.*;

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

    private int index;

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
            while (true) {
                synchronized (lock) {
                    rainbowHat.apa102.scrollRainbow(1);
                    rainbowHat.apa102.shift(0.01f);
                    for (int i = 0; i < RainbowHatState.NUMBER_OF_RGB_LEDS; i++) {
                        rainbowHat.boardView.setLedColor(i, rainbowHat.apa102.getColor(i));
                    }
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
                }
                if (rainbowHat.boardView != null) {
                    for (int i = 0; i < RainbowHatState.NUMBER_OF_RGB_LEDS; i++) {
                        rainbowHat.boardView.setLedColor(i, rainbowHat.apa102.getColor(i));
                    }
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
            index = 0;
            boolean invert = false;
            while (true) {
                synchronized (lock) {
                    for (int i = 0; i < data.length; i++) {
                        if (i == index) {
                            data[i][0] = (byte) 255;
                            data[i][1] = (byte) 0;
                            data[i][2] = (byte) 0;
                        } else {
                            data[i][0] = data[i][1] = data[i][2] = (byte) 0;
                        }
                    }
                    rainbowHat.apa102.setData(data);
                }
                if (rainbowHat.boardView != null) {
                    for (int i = 0; i < RainbowHatState.NUMBER_OF_RGB_LEDS; i++) {
                        rainbowHat.boardView.setLedColor(i, rainbowHat.apa102.getColor(i));
                    }
                }
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                }
                if (invert) {
                    index--;
                    if (index < 0) {
                        index = 0;
                        invert = false;
                    }
                } else {
                    index++;
                    if (index >= data.length) {
                        index = data.length - 1;
                        invert = true;
                    }
                }
                if (bouncingDotApaTask.isStopped()) {
                    break;
                }
            }
        });

    }

}