package org.concord.iot;

import java.awt.*;

/**
 * @author Charles Xie
 */

public final class Util {

    public static int rgbToInt(int r, int g, int b) {
        r = (r << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        g = (g << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        b = b & 0x000000FF; //Mask out anything not blue.
        return 0xFF000000 | r | g | b; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }

}
