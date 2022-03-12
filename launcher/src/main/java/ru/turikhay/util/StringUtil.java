package ru.turikhay.util;

import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.exceptions.ParseException;

public class StringUtil {

    public static boolean parseBoolean(String b) throws ParseException {
        if (b == null) {
            throw new ParseException("String cannot be NULL!");
        } else if (b.equalsIgnoreCase("true")) {
            return true;
        } else if (b.equalsIgnoreCase("false")) {
            return false;
        } else {
            throw new ParseException("Cannot parse value (" + b + ")!");
        }
    }

    public static String cut(String string, int max) {
        if (string == null) {
            return null;
        } else {
            int len = string.length();
            if (len <= max) {
                return string;
            } else {
                String[] words = string.split(" ");
                StringBuilder ret = new StringBuilder();
                int remaining = max + 1;

                for (int x = 0; x < words.length; ++x) {
                    String curword = words[x];
                    int curlen = curword.length();
                    if (curlen >= remaining) {
                        if (x == 0) {
                            ret.append(" ").append(curword, 0, remaining - 1);
                        }
                        break;
                    }

                    ret.append(" ").append(curword);
                    remaining -= curlen + 1;
                }

                return ret.length() == 0 ? "" : ret.substring(1) + "...";
            }
        }
    }

    public static String requireNotBlank(String s, String name) {
        if (s == null) {
            throw new NullPointerException(name);
        } else if (StringUtils.isBlank(s)) {
            throw new IllegalArgumentException(name);
        } else {
            return s;
        }
    }

    public static String requireNotBlank(String s) {
        return requireNotBlank(s, null);
    }

}
