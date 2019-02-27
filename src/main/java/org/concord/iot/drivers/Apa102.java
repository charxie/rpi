package org.concord.iot.drivers;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;

/**
 * APA 102 protocol: Start frame + LED1 + .... + LED7 + End frame
 * The start frame must be [0x00, 0x00, 0x00, 0x00] (32 bits).
 * The LED frames are in the BGR (not RGB) order.
 * Brightness is controlled by the first byte of a LED frame: 0b11100000 + brightness.
 * The BGR components are set by the following three bytes.
 * <p>
 * See also: https://cpldcpu.wordpress.com/2014/11/30/understanding-the-apa102-superled/
 * See also: https://hyperion-project.org/attachments/apa102_led-pdf.102/
 *
 * @author Charles Xie
 */

public class Apa102 {

    private final static byte[] START_FRAME = new byte[]{0, 0, 0, 0};

    private int numberOfPixels = 7;
    private SpiDevice spi;
    private byte brightness = 1; // from 0 to 31 (0 is completely out)
    private byte[][] data; // keep the data as the state of this driver

    public Apa102(int numberOfPixels) {
        this.numberOfPixels = numberOfPixels;
        try {
            spi = SpiFactory.getInstance(SpiChannel.CS0, SpiDevice.DEFAULT_SPI_SPEED, SpiDevice.DEFAULT_SPI_MODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        data = new byte[numberOfPixels + 1][4];
        for (int i = 0; i < data.length; i++) {
            updateData(i, Color.BLACK);
        }
    }

    public void setNumberOfPixels(int n) {
        byte[][] a = new byte[n + 1][4];
        for (int i = 0; i < a.length; i++) {
            if (i < data.length) {
                System.arraycopy(data[i], 0, a[i], 0, 4);
            } else {
                Arrays.fill(a[i], (byte) 0);
            }
        }
        setColorForAll(Color.BLACK);
        data = a;
        numberOfPixels = n;
        writeData();
    }

    private void writeData(){
        try {
            spi.write(START_FRAME); // start frame
            for (int i = 0; i < data.length; i++) {
                spi.write(data[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getNumberOfPixels() {
        return numberOfPixels;
    }

    public void setBrightness(byte brightness) {
        this.brightness = brightness;
    }

    public byte getBrightness() {
        return brightness;
    }

    // the alpha value should not use data[i][0] as it is for controlling brightness in a specific way
    public Color getColor(int i) {
        return new Color(data[i][3] & 0xFF, data[i][2] & 0xFF, data[i][1] & 0xFF, 255); // byte is signed
    }

    public void setColor(int led, Color color) {
        if (led < 0 || led >= data.length) return;
        updateData(led, color);
        writeData();
    }

    public void setData(byte[][] rgb) {
        for (int i = 0; i < rgb.length; i++) {
            data[i][0] = (byte) (0b11100000 + brightness); // brightness control
            data[i][1] = rgb[i][2];
            data[i][2] = rgb[i][1];
            data[i][3] = rgb[i][0];
        }
        writeData();
    }

    private void updateData(int i, Color color) {
        data[i][0] = (byte) (0b11100000 + brightness); // brightness control
        data[i][1] = (byte) color.getBlue();
        data[i][2] = (byte) color.getGreen();
        data[i][3] = (byte) color.getRed();
    }

    public void setColorForAll(Color color) {
        try {
            spi.write(START_FRAME); // start frame
            for (int i = 0; i <= numberOfPixels; i++) {
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

    public void setDefaultRainbow() {
        try {
            spi.write(START_FRAME); // start frame
            for (int i = 0; i <= numberOfPixels; i++) {
                updateData(i, Color.getHSBColor((float) i / (float) numberOfPixels, 1.0f, 1.0f));
                spi.write(data[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Need to call this from a thread
     */
    public void blinkAll(Color color, int times, int delay) {
        try {
            for (int i = 0; i < times; i++) {
                setColorForAll(color);
                Thread.sleep(delay);
                setColorForAll(Color.BLACK);
                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}