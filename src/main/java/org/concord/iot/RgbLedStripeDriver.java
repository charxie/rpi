package org.concord.iot;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

import java.io.IOException;

public class RgbLedStripeDriver {

    private static SpiDevice spi;

    public final static byte[] INIT = new byte[]{(byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000};
    public final static byte[] RED = new byte[]{(byte) 0b11111111, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b11111111};
    public final static byte[] GREEN = new byte[]{(byte) 0b11111111, (byte) 0b00000000, (byte) 0b11111111, (byte) 0b00000000};
    public final static byte[] BLUE = new byte[]{(byte) 0b11111111, (byte) 0b11111111, (byte) 0b00000000, (byte) 0b00000000};
    public final static byte[] BLACK = new byte[]{(byte) 0b11111111, (byte) 0b00000000, (byte) 0b00000000, (byte) 0b00000000};
    public final static byte[] WHITE = new byte[]{(byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111111};

    public RgbLedStripeDriver() {
        try {
            spi = SpiFactory.getInstance(SpiChannel.CS0, SpiDevice.DEFAULT_SPI_SPEED, SpiDevice.DEFAULT_SPI_MODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void turn(byte[] color, int pixels) {
        try {
            spi.write(INIT);
            for (int i = 0; i < pixels; i++) {
                spi.write(color);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void blink(byte[] color, int pixels, int times, int delay) {
        try {
            for (int i = 0; i < times; i++) {
                turn(BLACK, pixels);
                Thread.sleep(delay);
                turn(color, pixels);
                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}