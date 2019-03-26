package org.concord.iot;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.File;

/**
 * @author Charles Xie
 */

public final class Util {

    public static void sleepMilliseconds(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * If the user does not input the extension specified by the file filter, automatically augment the file name with the specified extension.
     */
    public static String fileNameAutoExtend(FileFilter filter, File file) {
        if (filter == null)
            return file.getAbsolutePath();
        String description = filter.getDescription().toLowerCase();
        String extension = getExtensionInLowerCase(file);
        String filename = file.getAbsolutePath();
        if (extension != null) {
            if (!filter.accept(file)) {
                filename = file.getAbsolutePath().concat(".").concat(description);
            }
        } else {
            filename = file.getAbsolutePath().concat(".").concat(description);
        }
        return filename;
    }

    /**
     * @return the extension of a file name
     */
    public static String getSuffix(String filename) {
        String extension = null;
        int index = filename.lastIndexOf('.');
        if (index >= 1 && index < filename.length() - 1) {
            extension = filename.substring(index + 1);
        }
        return extension;
    }

    /**
     * @return the extension of a file name in lower case
     */
    public static String getExtensionInLowerCase(File file) {
        if (file == null || file.isDirectory())
            return null;
        String extension = getSuffix(file.getName());
        if (extension != null)
            return extension.toLowerCase();
        return null;
    }

    public static int rgbToInt(int r, int g, int b) {
        r = (r << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        g = (g << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        b = b & 0x000000FF; //Mask out anything not blue.
        return 0xFF000000 | r | g | b; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }

    public static void setSelectedSilently(AbstractButton x, boolean b) {
        ActionListener[] al = x.getActionListeners();
        if (al != null && al.length > 0) {
            for (ActionListener a : al)
                x.removeActionListener(a);
        }
        ItemListener[] il = x.getItemListeners();
        if (il != null && il.length > 0) {
            for (ItemListener a : il)
                x.removeItemListener(a);
        }
        x.setSelected(b);
        if (al != null && al.length > 0) {
            for (ActionListener a : al)
                x.addActionListener(a);
        }
        if (il != null && il.length > 0) {
            for (ItemListener a : il)
                x.addItemListener(a);
        }
    }

}
