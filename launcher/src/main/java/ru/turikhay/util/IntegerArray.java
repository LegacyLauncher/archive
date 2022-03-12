package ru.turikhay.util;

import ru.turikhay.tlauncher.exceptions.ParseException;

import java.awt.*;

public class IntegerArray {
    public static final char defaultDelimiter = ';';
    private final int[] integers;
    private final char delimiter;
    private final int length;

    public IntegerArray(char del, int... values) {
        delimiter = del;
        length = values.length;
        integers = new int[length];
        System.arraycopy(values, 0, integers, 0, length);
    }

    public IntegerArray(int... values) {
        this(';', values);
    }

    public int get(int pos) {
        if (pos >= 0 && pos < length) {
            return integers[pos];
        } else {
            throw new ArrayIndexOutOfBoundsException("Invalid position (" + pos + " / " + length + ")!");
        }
    }

    public void set(int pos, int val) {
        if (pos >= 0 && pos < length) {
            integers[pos] = val;
        } else {
            throw new ArrayIndexOutOfBoundsException("Invalid position (" + pos + " / " + length + ")!");
        }
    }

    public int size() {
        return length;
    }

    public int[] toArray() {
        return integers.clone();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i : integers) {
            if (sb.length() != 0) {
                sb.append(delimiter);
            }

            sb.append(i);
        }

        return sb.toString();
    }

    public static IntegerArray parseIntegerArray(String val, char del) throws ParseException {
        if (val == null) {
            throw new ParseException("String cannot be NULL!");
        } else if (val.length() <= 1) {
            throw new ParseException("String mustn't equal or be less than delimiter!");
        } else {
            String regexp = "(?<!\\\\)";
            if (del != 120) {
                regexp = regexp + "\\";
            }

            regexp = regexp + del;
            String[] ints = val.split(regexp);
            int l = ints.length;
            int[] arr = new int[l];

            for (int i = 0; i < l; ++i) {
                int cur;
                try {
                    cur = Integer.parseInt(ints[i]);
                } catch (NumberFormatException var9) {
                    throw new ParseException("Cannot parse integer (iteration: " + i + ", del: \"" + del + "\")", var9);
                }

                arr[i] = cur;
            }

            return new IntegerArray(del, arr);
        }
    }

    public static IntegerArray parseIntegerArray(String val) throws ParseException {
        return parseIntegerArray(val, ';');
    }

    private static int[] toArray(String val, char del) throws ParseException {
        IntegerArray arr = parseIntegerArray(val, del);
        return arr.toArray();
    }

    public static int[] toArray(String val) throws ParseException {
        return toArray(val, ';');
    }

    public static IntegerArray fromDimension(Dimension d) {
        return new IntegerArray('x', d.width, d.height);
    }
}
