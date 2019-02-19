package org.concord.iot;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

import java.io.IOException;

public class RgbLedStripeSpi extends Thread {

    // Pi4J SPI device
    private static SpiDevice spi;

    // Stop semaphore.
    private volatile boolean stop;

    // Current values
    byte rainbowSegment = 0;
    byte r = 0;
    byte g = 0;
    byte b = 0;

    // Backup values stored at step X into loop, to enable smooth stepping
    byte bR = 0;
    byte bG = 0;
    byte bB = 0;
    byte bSegment = 0;

    // Start each sending of pixels with an "initialize" command
    byte[] init = new byte[]{(byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000};

    // Some hard coded colors for testing
    byte[] red = new byte[]{(byte) 0b11111111, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b11111111};
    byte[] green = new byte[]{(byte) 0b11111111, (byte) 0b00000000, (byte) 0b11111111, (byte) 0b00000000};
    byte[] blue = new byte[]{(byte) 0b11111111, (byte) 0b11111111, (byte) 0b00000000, (byte) 0b00000000};
    byte[] black = new byte[]{(byte) 0b11111111, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000};
    byte[] white = new byte[]{(byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111};


    public RgbLedStripeSpi() {
        try {
            spi = SpiFactory.getInstance(SpiChannel.CS0, SpiDevice.DEFAULT_SPI_SPEED, SpiDevice.DEFAULT_SPI_MODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void log(String msg) {
        System.out.println(msg);
    }

    private void turn(byte[] color, int pixels, int sleep) {
        try {
            spi.write(init);
            if (sleep > 0) Thread.sleep(sleep);
            for (int i = 0; i < pixels; i++) {
                spi.write(color);
                if (sleep > 0) Thread.sleep(sleep);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void blink(byte[] color, int times, int delay) {
        try {
            for (int i = 0; i < times; i++) {
                turn(black, 20, 0);
                Thread.sleep(delay);
                turn(color, 20, 0);
                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void rainbowNext(int drawFor) {
        byte backup = 1;
        byte step = 4;
        byte max = 0b00011111;
        byte min = 0b00000000;
        try {
            spi.write(init);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // At start of run, restore values from backups
        r = bR;
        g = bG;
        b = bB;
        rainbowSegment = bSegment;

        for (int i = 0; i < drawFor; i++) {
            // At iteration BACKUP, store backups of R,G,B.
            if (i == backup) {
                bR = r;
                bG = g;
                bB = b;
                bSegment = rainbowSegment;
            }
            if (rainbowSegment == 0) {
                r = max;
                g = (byte) Math.min(max, (g + step));
                b = min;
                if (g >= max) {
                    rainbowSegment = 1;
                }
            }
            if (rainbowSegment == 1) {
                r = (byte) Math.max(min, (r - step));
                g = max;
                b = min;
                if (r <= min) {
                    rainbowSegment = 2;
                }
            }
            if (rainbowSegment == 2) {
                r = min;
                g = max;
                b = (byte) Math.min(max, (b + step));
                if (b >= max) {
                    rainbowSegment = 3;
                }
            }
            if (rainbowSegment == 3) {
                r = min;
                g = (byte) Math.max(min, (g - step));
                b = max;
                if (g <= min) {
                    rainbowSegment = 4;
                }
            }
            if (rainbowSegment == 4) {
                r = (byte) Math.min(max, (r + step));
                g = min;
                b = max;
                if (r >= max) {
                    rainbowSegment = 5;
                }
            }
            if (rainbowSegment == 5) {
                r = max;
                g = min;
                b = (byte) Math.max(min, b - step);
                if (b <= min) {
                    rainbowSegment = 0;
                }
            }

            // log("S: " + rainbowSegment + " r=" + r + ",  g=" + g + ",  b=" + b);
            byte[] bb = new byte[4];
            bb[0] = (byte) 0b11111111;
            bb[1] = b;
            bb[2] = g;
            bb[3] = r;
            try {
                spi.write(bb);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }


    @Override
    public void run() {
        turn(blue, 8, 0);
        // Blink each RGB color and also white 3 times.
//        blink(red, 3, 500);
//        blink(green, 3, 500);
//        blink(blue, 3, 500);
//        blink(white, 3, 500);
//        try {
//            // Start a continuously rolling rainbow, stepping at 100 ms intervals.
//            while (!stop) {
//                rainbowNext(RainbowHatState.NUMBER_OF_LEDS_IN_STRIPE);
//                Thread.sleep(100);
//            }
//        } catch (Exception e) {
//            log("Fatal error in LED controller.\n" + e);
//            System.exit(1);
//        } finally {
//        }
    }

}