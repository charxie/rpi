package org.concord.iot.drivers;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

/*
 * Adopted from: https://github.com/ControlEverythingCommunity/VCNL4010/blob/master/Java/VCNL4010.java
 */

public class VCNL4010 {

    private int luminance;
    private int proximity;
    private I2CDevice device;

    public VCNL4010() throws IOException, I2CFactory.UnsupportedBusNumberException {
        I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
        device = bus.getDevice(0x13); // VCNL4010 I2C address is 0x13(19)
    }

    public void read() throws IOException, InterruptedException {

        // Select command register
        // Enables ALS and proximity measurement, LP oscillator
        device.write(0x80, (byte) 0xFF);
        // Select proximity rate register
        // 1.95 proximity measurement / s
        device.write(0x82, (byte) 0x00);
        // Select ALS register
        // Continuos conversion mode, ALS rate 2 samples / s
        device.write(0x84, (byte) 0x9D);
        Thread.sleep(800);

        // Read 4 bytes of data
        // luminance msb, luminance lsb, proximity msb, proximity lsb
        byte[] data = new byte[4];
        device.read(0x85, data, 0, 4);

        // Convert the data
        luminance = ((data[0] & 0xFF) * 256) + (data[1] & 0xFF);
        proximity = ((data[2] & 0xFF) * 256) + (data[3] & 0xFF);

    }

    public void printf() {
        System.out.printf("VCNL4010: Ambient Light Luminance : %d lux %n", luminance);
        System.out.printf("VCNL4010: Proximity Of The Device : %d %n", proximity);
    }

}