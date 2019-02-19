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
    private byte[][] data; // keep the data as the state of this driver

    public Apa102() {
        try {
            spi = SpiFactory.getInstance(SpiChannel.CS0, SpiDevice.DEFAULT_SPI_SPEED, SpiDevice.DEFAULT_SPI_MODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        data = new byte[RainbowHatState.NUMBER_OF_RGB_LEDS + 1][4];
        for (int i = 0; i < data.length; i++) {
            updateData(i, Color.BLACK);
        }
    }

    public Color getColor(int i) {
        return new Color(data[i][3] & 0xFF, data[i][2] & 0xFF, data[i][1] & 0xFF, data[i][0] & 0xFF); // byte is signed
    }

    public void setColor(int led, Color color) {
        if (led < 0 || led >= data.length) return;
        updateData(led, color);
        try {
            spi.write(START_FRAME); // start frame
            for (int i = 0; i <= RainbowHatState.NUMBER_OF_RGB_LEDS; i++) {
                spi.write(data[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateData(int i, Color color) {
        data[i][0] = (byte) color.getAlpha();
        data[i][1] = (byte) color.getBlue();
        data[i][2] = (byte) color.getGreen();
        data[i][3] = (byte) color.getRed();
    }

    public void setColorForAll(Color color) {
        try {
            spi.write(START_FRAME); // start frame
            for (int i = 0; i <= RainbowHatState.NUMBER_OF_RGB_LEDS; i++) {
                updateData(i, color);
                spi.write(data[i]);
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

}