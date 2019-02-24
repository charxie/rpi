package org.concord.iot;

import java.util.HashMap;
import java.util.Map;

/**
 * Source: https://gist.github.com/yngwie74/2025449
 */

class LcdFont {

    private static final String _NONE = "   ";
    private static final String _LEFT = "  |";
    private static final String _MIDL = " _ ";
    private static final String _MDLT = " _|";
    private static final String _MDRT = "|_ ";
    private static final String _FULL = "|_|";
    private static final String _BOTH = "| |";

    private static final Map<Integer, String[]> SEGMENTS = new HashMap<Integer, String[]>() {
        {
            put(1, new String[]{_NONE, _LEFT, _LEFT});
            put(2, new String[]{_MIDL, _MDLT, _MDRT});
            put(3, new String[]{_MIDL, _MDLT, _MDLT});
            put(4, new String[]{_NONE, _FULL, _LEFT});
            put(5, new String[]{_MIDL, _MDRT, _MDLT});
            put(6, new String[]{_MIDL, _MDRT, _FULL});
            put(7, new String[]{_MIDL, _LEFT, _LEFT});
            put(8, new String[]{_MIDL, _FULL, _FULL});
            put(9, new String[]{_MIDL, _FULL, _MDLT});
            put(0, new String[]{_MIDL, _BOTH, _FULL});
        }
    };

    public static String format(int number) {
        String[][] segments = getSegmentsForEachDigit(number);
        String[] result = arrangeHorizontally(segments);
        return toTextLines(result);
    }

    private static String toTextLines(String[] result) {
        return join(result, '\n');
    }

    private static String[][] getSegmentsForEachDigit(int number) {
        String digits = Integer.toString(number);
        String[][] result = new String[digits.length()][];
        for (int i = 0; i < digits.length(); i++) {
            result[i] = segmentsFor(digitAt(digits, i));
        }
        return result;
    }

    private static int digitAt(String digits, int i) {
        return Integer.parseInt(Character.toString(digits.charAt(i)));
    }

    private static String[] segmentsFor(int number) {
        String result[] = SEGMENTS.get(new Integer(number));
        if (null == result)
            throw new RuntimeException(String.format("Dígito %d no encontrado", number));
        return result;
    }

    private static String join(String[] strings, char delim) {
        StringBuffer sb = new StringBuffer();
        for (String string : strings) {
            if (sb.length() > 0)
                sb.append(delim);
            sb.append(string);
        }
        return sb.toString();
    }

    private static String[] arrangeHorizontally(String[][] data) {
        assert data.length > 0;
        String[] result = data[0].clone();
        for (int row = 1; row < data.length; row++) {
            for (int col = 0; col < data[row].length; col++)
                result[col] += data[row][col];
        }
        return result;
    }

}