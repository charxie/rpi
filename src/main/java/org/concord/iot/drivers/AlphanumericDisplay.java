package org.concord.iot.drivers;

/*
 * #%L
 * %%
 * Copyright (C) 2016
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * Java interface to the "Adafruit 0.54 inch Alphanumeric FeatherWing Display". A detailed
 * description of this 4-character LED Display is given here:
 * <a href="https://www.adafruit.com/products/3130">See FeatherWing Display </a>
 *
 * @author Eric Eliason
 */
public class AlphanumericDisplay {

    /*
     * This char array provides the bit-font for most alphanumeric characters
     * in the ASCII table. The font array was extracted from the Adafruit_LED_Backpack
     * software available here: https://github.com/adafruit/Adafruit_LED_Backpack
     *
     */
    final static char[] FONT = new char[]{
            0b0000000000000001,
            0b0000000000000010,
            0b0000000000000100,
            0b0000000000001000,
            0b0000000000010000,
            0b0000000000100000,
            0b0000000001000000,
            0b0000000010000000,
            0b0000000100000000,
            0b0000001000000000,
            0b0000010000000000,
            0b0000100000000000,
            0b0001000000000000,
            0b0010000000000000,
            0b0100000000000000,
            0b1000000000000000,
            0b0000000000000000,
            0b0000000000000000,
            0b0000000000000000,
            0b0000000000000000,
            0b0000000000000000,
            0b0000000000000000,
            0b0000000000000000,
            0b0000000000000000,
            0b0001001011001001,
            0b0001010111000000,
            0b0001001011111001,
            0b0000000011100011,
            0b0000010100110000,
            0b0001001011001000,
            0b0011101000000000,
            0b0001011100000000,
            0b0000000000000000, //
            0b0000000000000110, // !
            0b0000001000100000, // "
            0b0001001011001110, // #
            0b0001001011101101, // $
            0b0000110000100100, // %
            0b0010001101011101, // &
            0b0000010000000000, // '
            0b0010010000000000, // (
            0b0000100100000000, // )
            0b0011111111000000, // *
            0b0001001011000000, // +
            0b0000100000000000, // ,
            0b0000000011000000, // -
            0b0000000000000000, // .
            0b0000110000000000, // /
            0b0000110000111111, // 0
            0b0000000000000110, // 1
            0b0000000011011011, // 2
            0b0000000010001111, // 3
            0b0000000011100110, // 4
            0b0010000001101001, // 5
            0b0000000011111101, // 6
            0b0000000000000111, // 7
            0b0000000011111111, // 8
            0b0000000011101111, // 9
            0b0001001000000000, // :
            0b0000101000000000, // ;
            0b0010010000000000, // <
            0b0000000011001000, // =
            0b0000100100000000, // >
            0b0001000010000011, // ?
            0b0000001010111011, // @
            0b0000000011110111, // A
            0b0001001010001111, // B
            0b0000000000111001, // C
            0b0001001000001111, // D
            0b0000000011111001, // E
            0b0000000001110001, // F
            0b0000000010111101, // G
            0b0000000011110110, // H
            0b0001001000000000, // I
            0b0000000000011110, // J
            0b0010010001110000, // K
            0b0000000000111000, // L
            0b0000010100110110, // M
            0b0010000100110110, // N
            0b0000000000111111, // O
            0b0000000011110011, // P
            0b0010000000111111, // Q
            0b0010000011110011, // R
            0b0000000011101101, // S
            0b0001001000000001, // T
            0b0000000000111110, // U
            0b0000110000110000, // V
            0b0010100000110110, // W
            0b0010110100000000, // X
            0b0001010100000000, // Y
            0b0000110000001001, // Z
            0b0000000000111001, // [
            0b0010000100000000, //
            0b0000000000001111, // ]
            0b0000110000000011, // ^
            0b0000000000001000, // _
            0b0000000100000000, // `
            0b0001000001011000, // a
            0b0010000001111000, // b
            0b0000000011011000, // c
            0b0000100010001110, // d
            0b0000100001011000, // e
            0b0000000001110001, // f
            0b0000010010001110, // g
            0b0001000001110000, // h
            0b0001000000000000, // i
            0b0000000000001110, // j
            0b0011011000000000, // k
            0b0000000000110000, // l
            0b0001000011010100, // m
            0b0001000001010000, // n
            0b0000000011011100, // o
            0b0000000101110000, // p
            0b0000010010000110, // q
            0b0000000001010000, // r
            0b0010000010001000, // s
            0b0000000001111000, // t
            0b0000000000011100, // u
            0b0010000000000100, // v
            0b0010100000010100, // w
            0b0010100011000000, // x
            0b0010000000001100, // y
            0b0000100001001000, // z
            0b0000100101001001, // {
            0b0001001000000000, // |
            0b0010010010001001, // }
            0b0000010100100000, // ~
            0b0011111111111111};

    //Always use I2C BUS 1
    protected final int I2C_BUS = I2CBus.BUS_1;
    //The default Feather Wing Device i2c address is 70
    protected final int I2C_DEVICE = 0X70;

    protected I2CBus i2cBus;
    protected I2CDevice adafruitFeather;

    //Specifies blink control on LED Display
    protected HT16K33 blink = HT16K33.BLINK_OFF;
    //Specifies brightness control on LED Display
    protected HT16K33 brightness = HT16K33.DUTY_16;

    //fixed values for writing HT16K33 display buffer
    protected final int nBytes = 17;
    protected final int offset = 0;

    /**
     * Constructor. Set up the I2C Bus and the I2C device.
     * Turn on the HT16K33 chip, turn on the display, set
     * the blink rate, and finally set display brightness.
     *
     * @param blink      (HT16K33.BLINK_1HZ, HT16K33.BLINK_2HZ, BLINK_HALFHZ, HT16K33.BLINK_OFF)
     * @param brightness (HT16K33.DUTY_01 thru HT16K33.DUTY_16)
     */
    public AlphanumericDisplay(HT16K33 blink, HT16K33 brightness) {

        if (blink != HT16K33.BLINK_1HZ &&
                blink != HT16K33.BLINK_2HZ &&
                blink != HT16K33.BLINK_HALFHZ &&
                blink != HT16K33.BLINK_OFF) {
            System.out.println("*** ERROR *** Invalid blink rate specified");
            System.exit(-1);
        }

        if (brightness != HT16K33.DUTY_01 && brightness != HT16K33.DUTY_02 && brightness != HT16K33.DUTY_03 &&
                brightness != HT16K33.DUTY_04 && brightness != HT16K33.DUTY_05 && brightness != HT16K33.DUTY_06 &&
                brightness != HT16K33.DUTY_07 && brightness != HT16K33.DUTY_08 && brightness != HT16K33.DUTY_09 &&
                brightness != HT16K33.DUTY_10 && brightness != HT16K33.DUTY_11 && brightness != HT16K33.DUTY_12 &&
                brightness != HT16K33.DUTY_13 && brightness != HT16K33.DUTY_14 && brightness != HT16K33.DUTY_15 &&
                brightness != HT16K33.DUTY_16) {
            System.out.println("*** ERROR *** Invalid LED Brightness specified");
            System.exit(-1);
        }

        this.blink = blink;
        this.brightness = brightness;

        try {
            i2cBus = I2CFactory.getInstance(I2C_BUS);
            adafruitFeather = i2cBus.getDevice(I2C_DEVICE);

            //turn on oscillator wakes up the HT16K33 chip
            adafruitFeather.write((byte) (HT16K33.CMD_SYSTEM_SETUP.get() | HT16K33.OSCILLATOR_ON.get()));
            Thread.sleep(10);

            //turn on all output rows of the HT16K33 chip (even though were only using 8 of them)
            adafruitFeather.write((byte) (HT16K33.CMD_ROW_INT_SET.get()));

        } catch (UnsupportedBusNumberException | IOException | InterruptedException e) {
            System.out.println("*** Error *** failed to connect with I2C Bus or I2C Device");
            e.printStackTrace();
        }

        //clear the display
        displayClear();

        //set the blink rate
        setBlinkMode(blink);

        //set the brightness level
        setBrigtness(brightness);
    }

    /**
     * Clear the LED display buffer--the four characters are blank
     */
    public void displayClear() {
        //Used to zero out HT16K33 display buffer
        byte[] zeroDisplay = new byte[]{HT16K33.CMD_DISPLAY_POINTER.get(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        try {
            adafruitFeather.write(zeroDisplay, offset, nBytes);
        } catch (IOException e) {
            System.out.println("*** Error *** failed to clear 4-character LED Display");
            e.printStackTrace();
        }
    }

    /**
     * Turn on the LED display
     */
    public void displayOn() {
        //set the blink rate and turn on the LED display
        try {
            adafruitFeather.write((byte) (HT16K33.CMD_DISPLAY_SETUP.get() | blink.get() | HT16K33.DISPLAY_ON.get()));
        } catch (IOException e) {
            System.out.println("*** Error *** failed to turn on LED display");
            e.printStackTrace();
        }
    }

    /**
     * Turn off the LED display. The display buffer is not cleared so
     * if you turn on the display again then contents of the display buffer
     * reappear .
     */
    public void displayOff() {
        //set the blink rate and turn on the LED display
        try {
            adafruitFeather.write((byte) (HT16K33.CMD_DISPLAY_SETUP.get() | blink.get() | HT16K33.DISPLAY_OFF.get()));
        } catch (IOException e) {
            System.out.println("*** ERROR *** failed to turn off LED display");
            e.printStackTrace();
        }
    }

    /**
     * Turning on the oscillator has the effect of taking the HT16K33 out of
     * standby mode. Turned on it is possible to then
     * command the LED display.
     */
    public void oscillatorOn() {
        //turn on oscillator wakes up the HT16K33 chip
        try {
            adafruitFeather.write((byte) (HT16K33.CMD_SYSTEM_SETUP.get() | HT16K33.OSCILLATOR_ON.get()));
            Thread.sleep(100);
        } catch (IOException | InterruptedException e) {
            System.out.println("*** ERROR *** failed to turn on oscillator");
            e.printStackTrace();
        }
    }

    /**
     * Turning off the oscillator has the effect of putting the HT16K33 into
     * standby mode. Turned off it is not possible to then
     * command the LED display until it is turned back on again.
     */
    public void oscillatorOff() {
        try {
            adafruitFeather.write((byte) (HT16K33.CMD_SYSTEM_SETUP.get() | HT16K33.OSCILLATOR_OFF.get()));
            Thread.sleep(100);
        } catch (IOException | InterruptedException e) {
            System.out.println("*** ERROR *** failed to turn off oscillator");
            e.printStackTrace();
        }
    }

    /**
     * Set the blink mode on the LED display and turn the display on.
     *
     * @param blink (HT16K33.BLINK_1HZ, HT16K33.BLINK_2HZ, BLINK_HALFHZ, HT16K33.BLINK_OFF)
     */
    public void setBlinkMode(HT16K33 blink) {
        if (blink != HT16K33.BLINK_1HZ &&
                blink != HT16K33.BLINK_2HZ &&
                blink != HT16K33.BLINK_HALFHZ &&
                blink != HT16K33.BLINK_OFF) {
            System.out.println("*** ERROR *** Invalid blink rate specified");
            System.exit(-1);
        }
        this.blink = blink;
        //set the blink rate and turn on the LED display
        try {
            adafruitFeather.write((byte) (HT16K33.CMD_DISPLAY_SETUP.get() | this.blink.get() | HT16K33.DISPLAY_ON.get()));
        } catch (IOException e) {
            System.out.println("*** ERROR *** failed to set blink mode");
            e.printStackTrace();
        }
    }

    /**
     * Set the brightness level of the LED display.
     *
     * @param brightness (HT16K33.DUTY_01 thru HT16K33.DUTY_16)
     */
    public void setBrigtness(HT16K33 brightness) {
        if (brightness != HT16K33.DUTY_01 && brightness != HT16K33.DUTY_02 && brightness != HT16K33.DUTY_03 &&
                brightness != HT16K33.DUTY_04 && brightness != HT16K33.DUTY_05 && brightness != HT16K33.DUTY_06 &&
                brightness != HT16K33.DUTY_07 && brightness != HT16K33.DUTY_08 && brightness != HT16K33.DUTY_09 &&
                brightness != HT16K33.DUTY_10 && brightness != HT16K33.DUTY_11 && brightness != HT16K33.DUTY_12 &&
                brightness != HT16K33.DUTY_13 && brightness != HT16K33.DUTY_14 && brightness != HT16K33.DUTY_15 &&
                brightness != HT16K33.DUTY_16) {
            System.out.println("*** ERROR *** Invalid LED Brightness specified");
            System.exit(-1);
        }
        this.brightness = brightness;
        //set the brightness level of the LED display
        try {
            adafruitFeather.write((byte) (HT16K33.CMD_BRIGHTNESS_SET.get() | brightness.get()));
        } catch (IOException e) {
            System.out.println("*** ERROR *** failed to set display brigtness");
            e.printStackTrace();
        }

    }

    /**
     * Display the passed four-character string on the LED Display
     *
     * @param displayString - must be a 4 character string.
     */
    public void display(String displayString) {
        /*
         * displayBuffer contents are
         * [0]    = the command for transferring 16-bytes to HT16K33 row pins (CMD_DISPLAY_POINTER)
         * [1-8]  = bit font configuration for the 4-character display (2 bytes per digit)
         * [9-16] = always 0 because Adafruit feather wing only uses first 8.
         */
        byte[] displayBuffer = new byte[17];

        //index to the display buffer
        int displayIndex = 0;

        // The passed displayString must be exactly 4 characters in length.
        if (displayString.length() != 4) {
            System.out.println("*** Error *** The displayString passed must be exactly 4 characters! -- " + displayString);
            System.out.println("displayString length found: " + displayString.length());
            System.exit(-1);
        }

        // 1st byte is the display command
        displayBuffer[displayIndex++] = HT16K33.CMD_DISPLAY_POINTER.get();

        //The next 8 bytes define the bit font for the 4 characters
        char[] digits = displayString.toCharArray();
        for (char digit : digits) {
            displayBuffer[displayIndex++] = (byte) (FONT[digit]);
            displayBuffer[displayIndex++] = (byte) (FONT[digit] >> 8);
        }

        //fill remaining bytes with zeros
        for (int i = 0; i < 8; i++) displayBuffer[displayIndex++] = 0;

        //display the 4 characters
        try {
            adafruitFeather.write(displayBuffer, offset, nBytes);
        } catch (IOException e) {
            System.out.println("*** Error *** trying to write to HT16K33 Display buffer");
            e.printStackTrace();
        }
    }

    /**
     * enum HT16K33 defines the commanding and parameter options for the HOLTEK HT16K33 chip.
     * RAM Mapping 16*8 LED Controller Driver and Keyscan. For detailed information see:
     * <a href="https://cdn-shop.adafruit.com/datasheets/ht16K33v110.pdf">See HT16K33</a>
     *
     * @author Eric ELiason
     */
    public enum HT16K33 {
        //Commands
        CMD_DISPLAY_POINTER((byte) 0X00),
        CMD_SYSTEM_SETUP((byte) 0X20),
        CMD_KEY_DATA_POINTER((byte) 0X40),
        CMD_INT_FLAG_POINTER((byte) 0X60),
        CMD_DISPLAY_SETUP((byte) 0X80),
        CMD_ROW_INT_SET((byte) 0XA0),
        CMD_BRIGHTNESS_SET((byte) 0XE0),
        //Parameters for CMD_SYSTEM_SETUP
        OSCILLATOR_OFF((byte) 0X00),
        OSCILLATOR_ON((byte) 0X01),
        //Parameters for CMD_DISPLAY_SETUP
        DISPLAY_OFF((byte) 0X00),
        DISPLAY_ON((byte) 0X01),
        BLINK_OFF((byte) 0X00),
        BLINK_2HZ((byte) 0X02),
        BLINK_1HZ((byte) 0X04),
        BLINK_HALFHZ((byte) 0X06),
        //Parameters for CMD_DIMMING_SET, specifies amount of LED dimming
        DUTY_01((byte) 0X00),
        DUTY_02((byte) 0X01),
        DUTY_03((byte) 0X02),
        DUTY_04((byte) 0X03),
        DUTY_05((byte) 0X04),
        DUTY_06((byte) 0X05),
        DUTY_07((byte) 0X06),
        DUTY_08((byte) 0X07),
        DUTY_09((byte) 0X08),
        DUTY_10((byte) 0X09),
        DUTY_11((byte) 0X0A),
        DUTY_12((byte) 0X0B),
        DUTY_13((byte) 0X0C),
        DUTY_14((byte) 0X0D),
        DUTY_15((byte) 0X0E),
        DUTY_16((byte) 0X0F);

        private byte b;

        private HT16K33(byte b) {
            this.b = b;
        }

        public byte get() {
            return b;
        }
    }

}