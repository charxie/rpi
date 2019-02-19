package org.concord.iot;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

import java.awt.*;
import java.io.IOException;

/**
 * APA 102 protocol: Start frame + LED1 + .... + LED7 + End frame
 * The start frame must be [0x00, 0x00, 0x00, 0x00] (32 bits). The LED frames are in the BGR (not RGB) order.
 * Brightness is controlled by the first byte: 0xE0+brightness.
 * See: https://cpldcpu.wordpress.com/2014/11/30/understanding-the-apa102-superled/
 *
 * @author Charles Xie
 */

public class Apa102 {

    private final static byte[] START_FRAME = new byte[]{0, 0, 0, 0};

    private SpiDevice spi;
    private int brightness = 1;

    public Apa102() {
        try {
            spi = SpiFactory.getInstance(SpiChannel.CS0, SpiDevice.DEFAULT_SPI_SPEED, SpiDevice.DEFAULT_SPI_MODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setColor(int i, Color color){

    }

    public void setColorForAll(Color color) {
        try {
            spi.write(START_FRAME); // start frame
            for (int i = 0; i <= RainbowHatState.NUMBER_OF_LEDS_IN_STRIPE; i++) {
                spi.write(toABGR(color));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void turnoff() {
        setColorForAll(Color.BLACK);
    }

    public void blinkAll(Color color, int times, int delay) {
        try {
            for (int i = 0; i < times; i++) {
                setColorForAll(Color.BLACK);
                Thread.sleep(delay);
                setColorForAll(color);
                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static byte[] toABGR(Color color) {
        return new byte[]{(byte) color.getAlpha(), (byte) color.getBlue(), (byte) color.getGreen(), (byte) color.getRed()};
    }

}