package org.concord.iot.drivers;

/**
 * Wide supply voltage, 1.71 V to 3.6 V
 * Independent IOs supply (1.8 V) and supply voltage compatible
 * Ultra low-power mode consumption down to 2 microA
 * +- (2g/4g/8g/16g) dynamically selectable fullscale
 * I2C/SPI digital output interface
 * 16 bit data output
 * 2 independent programmable interrupt generators for free-fall and motion detection
 * 6D/4D orientation detection
 * Free-fall detection
 *
 * @author Yadwinder Singh
 * @author Charles Xie
 */

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

public class LIS3DH {

    public final static int LIS3DHTR_CTRL_REG1_A = 0x20;
    public final static int LIS3DHTR_CTRL_REG2_A = 0x21;
    public final static int LIS3DHTR_CTRL_REG3_A = 0x22;
    public final static int LIS3DHTR_CTRL_REG4_A = 0x23;
    public final static int LIS3DHTR_CTRL_REG5_A = 0x24;

    private int ax = 0;
    private int ay = 0;
    private int az = 0;
    private double angularAboutX = 0;
    private double angularAboutY = 0;
    private double pitch;
    private double roll;
    private I2CDevice device;

    public LIS3DH() throws IOException, I2CFactory.UnsupportedBusNumberException {
        I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
        device = bus.getDevice(0x18);
        // sets up the accelerometer to begin reading. Normal operation mode, all axes enabled. 10 Hz ODR Data Rate
        device.write(LIS3DHTR_CTRL_REG1_A, (byte) 0x27);
        device.write(LIS3DHTR_CTRL_REG2_A, (byte) 0x00);
        device.write(LIS3DHTR_CTRL_REG3_A, (byte) 0x00);
        device.write(LIS3DHTR_CTRL_REG4_A, (byte) 0x00);
        device.write(LIS3DHTR_CTRL_REG5_A, (byte) 0x00);
    }

    public void read() throws IOException {

        byte[] b = new byte[6];
        b[0] = (byte) device.read(0x28);
        b[1] = (byte) device.read(0x29);
        b[2] = (byte) device.read(0x2A);
        b[3] = (byte) device.read(0x2B);
        b[4] = (byte) device.read(0x2C);
        b[5] = (byte) device.read(0x2D);

        ax = (b[1] << 8) | (b[0] & 0xFF);
        ay = (b[3] << 8) | (b[2] & 0xFF);
        az = (b[5] << 8) | (b[4] & 0xFF);

        angularAboutX = (Math.atan2(ay, az) + 3.14) * 57.3;
        angularAboutY = (Math.atan2(az, ax) + 3.14) * 57.3;

        pitch = Math.toDegrees(Math.atan2(-ax, Math.hypot(ay, az)));
        roll = Math.toDegrees(Math.atan2(ay, az));

    }

    public void printf() {
        System.out.printf("LIS3DH: Acceleration : %d, %d, %d %n", ax, ay, az);
        System.out.printf("LIS3DH: Pitch: %.2f, Roll: %.2f %n", pitch, roll);
    }

    public double getPitch() {
        return pitch;
    }

    public double getRoll() {
        return roll;
    }

    public int getAx() {
        return ax;
    }

    public int getAy() {
        return ay;
    }

    public int getAz() {
        return az;
    }

    public double getAngularAboutX() {
        return angularAboutX;
    }

    public double getAngularAboutY() {
        return angularAboutY;
    }

}