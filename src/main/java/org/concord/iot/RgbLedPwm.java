package org.concord.iot;

import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.SoftPwm;

public class RgbLedPwm {

    private final static int PIN_NUMBER_RED = 0;
    private final static int PIN_NUMBER_GREEN = 1;
    private final static int PIN_NUMBER_BLUE = 2;

    private static int colors[] = {0xFF0000, 0x00FF00, 0x0000FF, 0xFFFF00, 0x00FFFF, 0xFF00FF, 0xFFFFFF, 0x9400D3};

    private static int map(int x, int in_min, int in_max, int out_min, int out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    private void setColor(int color) {
        int r = (color & 0xFF0000) >> 16;
        int g = (color & 0x00FF00) >> 8;
        int b = (color & 0x0000FF) >> 0;

        r = map(r, 0, 255, 0, 100); // change a num(0~255) to 0~100
        g = map(g, 0, 255, 0, 100);
        b = map(b, 0, 255, 0, 100);

        SoftPwm.softPwmWrite(PIN_NUMBER_RED, 100 - r); // change duty cycle
        SoftPwm.softPwmWrite(PIN_NUMBER_GREEN, 100 - g);
        SoftPwm.softPwmWrite(PIN_NUMBER_BLUE, 100 - b);
    }

    private void init() {
        Gpio.wiringPiSetup(); // initialize the wiringPi library, this is needed for PWM
        SoftPwm.softPwmCreate(PIN_NUMBER_RED, 0, 100);
        SoftPwm.softPwmCreate(PIN_NUMBER_GREEN, 0, 100);
        SoftPwm.softPwmCreate(PIN_NUMBER_BLUE, 0, 100);
    }

}