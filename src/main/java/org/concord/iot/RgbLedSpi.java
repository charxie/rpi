package org.concord.iot;


import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

public class RgbLedSpi extends Thread {

    // How many APA102 pixels we have
    private static final int PIXELS = 7;

    // Pi4J SPI device
    public static SpiDevice spi = null;

    // Stop semaphore.
    public volatile boolean stop = false;

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
    byte init[] = new byte[] {
            (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000,
    };

    // Some hard coded colors for testing

    byte red[] = new byte[] {
            (byte) 0b11111111, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b11111111,
    };

    byte green[] = new byte[] {
            (byte) 0b11111111, (byte) 0b00000000, (byte) 0b11111111, (byte) 0b00000000,
    };

    byte blue[] = new byte[] {
            (byte) 0b11111111, (byte) 0b11111111, (byte) 0b00000000, (byte) 0b00000000,
    };

    byte black[] = new byte[] {
            (byte) 0b11111111, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000,
    };

    byte white[] = new byte[] {
            (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111,
    };



    public RgbLedSpi() {

        try {
            log("Getting SPI instance...");
            spi = SpiFactory.getInstance(SpiChannel.CS0,
                    SpiDevice.DEFAULT_SPI_SPEED, // According to Pi4J: default spi speed 1 MHz
                    SpiDevice.DEFAULT_SPI_MODE); // According to Pi4J: default spi mode 0

        } catch (Exception e) {

            log("Failed getting SPI instance.");
            e.printStackTrace();

        }
    }


    private void log(String msg) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void rainbowNext(int drawFor) {

        byte backup = 1;
        byte step = 4;
        byte max = 0b00011111;
        byte min = 0b00000000;

        try { spi.write(init); } catch (Exception e) { e.printStackTrace(); }

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
                r = (byte) max;
                g = (byte) Math.min(max,(g + step));
                b = (byte) min;
                if (g >= max) {
                    rainbowSegment = 1;
                }
            }
            if (rainbowSegment == 1) {
                r = (byte) Math.max(min, (r - step));
                g = (byte) max;
                b = (byte) min;
                if (r <= min) {
                    rainbowSegment = 2;
                }
            }
            if (rainbowSegment == 2) {
                r = (byte) min;
                g = (byte) max;
                b = (byte) Math.min(max,(b + step));
                if (b >= max) {
                    rainbowSegment = 3;
                }
            }
            if (rainbowSegment == 3) {
                r = (byte) min;
                g = (byte) Math.max(min, (g - step));
                b = (byte) max;
                if (g <= min) {
                    rainbowSegment = 4;
                }
            }
            if (rainbowSegment == 4) {
                r = (byte) Math.min(max,(r + step));
                g = (byte) min;
                b = (byte) max;
                if (r >= max) {
                    rainbowSegment = 5;
                }
            }
            if (rainbowSegment == 5) {
                r = (byte) max;
                g = (byte) min;
                b = (byte) Math.max(min,b - step);
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
            try { spi.write(bb); } catch (Exception e) { e.printStackTrace(); }
        }
        // log("================================================");
    }


    @Override
    public void run() {

        // Blink each RGB color and also white 3 times.
        blink(red,3,500);
        blink(green,3,500);
        blink(blue,3,500);
        blink(white,3,500);


        try {
            // Start a continuously rolling rainbow, stepping at 100 ms intervals.
            while (!stop) {
                rainbowNext(PIXELS);
                Thread.sleep(100);
            }

        } catch (Exception e) {
            log("Fatal error in LED controller.\n" + e);
            System.exit(1);
        } finally {

        }
    }

}