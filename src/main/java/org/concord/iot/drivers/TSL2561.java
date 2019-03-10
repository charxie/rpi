package org.concord.iot.drivers;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

/**
 * Adopted from https://github.com/ControlEverythingCommunity/TSL2561/blob/master/Java/TSL2561.java
 */

public class TSL2561 {

    private double fullLux;
    private double infraredLux;
    private I2CDevice device;

    public TSL2561() throws IOException, I2CFactory.UnsupportedBusNumberException {
        I2CBus Bus = I2CFactory.getInstance(I2CBus.BUS_1);
        device = Bus.getDevice(0x39); // TSL2561 I2C address is 0x39(57)
    }

    public void read() throws IOException {

        // Select control register
        // Power ON mode
        device.write(0x00 | 0x80, (byte) 0x03);
        // Select timing register
        // Nominal integration time = 402ms
        device.write(0x01 | 0x80, (byte) 0x02);

        // Read 4 bytes of data
        // ch0 lsb, ch0 msb, ch1 lsb, ch1 msb
        byte[] data = new byte[4];
        device.read(0x0C | 0x80, data, 0, 4);

        // Convert the data
        fullLux = ((data[1] & 0xFF) * 256 + (data[0] & 0xFF));
        infraredLux = ((data[3] & 0xFF) * 256 + (data[2] & 0xFF));

    }

    public void printf() {
        System.out.printf("TSL2561: Full Spectrum(IR + Visible) : %.2f lux %n", fullLux);
        System.out.printf("TSL2561: Infrared Value : %.2f lux %n", infraredLux);
        System.out.printf("TSL2561: Visible Value : %.2f lux %n", fullLux - infraredLux);
    }

    public double getFullLux() {
        return fullLux;
    }

    public double getVisibleLux() {
        return fullLux - infraredLux;
    }

    public double getInfraredLux() {
        return infraredLux;
    }

}