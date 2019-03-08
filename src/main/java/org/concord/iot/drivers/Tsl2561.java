package org.concord.iot.drivers;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

public class Tsl2561 {

    private double full;
    private double infrared;
    private I2CDevice device;

    public Tsl2561() throws IOException, I2CFactory.UnsupportedBusNumberException {
        I2CBus Bus = I2CFactory.getInstance(I2CBus.BUS_1);
        device = Bus.getDevice(0x39); // TSL2561 I2C address is 0x39(57)
    }

    public void read() throws IOException, InterruptedException {

        // Select control register
        // Power ON mode
        device.write(0x00 | 0x80, (byte) 0x03);
        // Select timing register
        // Nominal integration time = 402ms
        device.write(0x01 | 0x80, (byte) 0x02);
        Thread.sleep(500);

        // Read 4 bytes of data
        // ch0 lsb, ch0 msb, ch1 lsb, ch1 msb
        byte[] data = new byte[4];
        device.read(0x0C | 0x80, data, 0, 4);

        // Convert the data
        full = ((data[1] & 0xFF) * 256 + (data[0] & 0xFF));
        infrared = ((data[3] & 0xFF) * 256 + (data[2] & 0xFF));

    }

    public void printf() {
        System.out.printf("Full Spectrum(IR + Visible) : %.2f lux %n", full);
        System.out.printf("Infrared Value : %.2f lux %n", infrared);
        System.out.printf("Visible Value : %.2f lux %n", full - infrared);
    }

    public double getFull() {
        return full;
    }

    public double getVisible() {
        return full - infrared;
    }

    public double getInfrared() {
        return infrared;
    }

}