package org.apache.commons.lang3.text;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public abstract class StrMatcher {

    private static final StrMatcher COMMA_MATCHER = new CharMatcher(',');
    private static final StrMatcher TAB_MATCHER = new CharMatcher('\t');
    private static final StrMatcher SPACE_MATCHER = new CharMatcher(' ');
    private static final StrMatcher SPLIT_MATCHER = new CharSetMatcher(" \t\n\r\f".toCharArray());
    private static final StrMatcher TRIM_MATCHER = new TrimMatcher();
    private static final StrMatcher SINGLE_QUOTE_MATCHER = new CharMatcher('\'');
    private static final StrMatcher DOUBLE_QUOTE_MATCHER = new CharMatcher('"');
    private static final StrMatcher QUOTE_MATCHER = new CharSetMatcher("'\"".toCharArray());
    private static final StrMatcher NONE_MATCHER = new NoMatcher();
    
    public static StrMatcher commaMatcher() {
        return COMMA_MATCHER;
    }

    public static StrMatcher tabMatcher() {
        return TAB_MATCHER;
    }

    public static StrMatcher spaceMatcher() {
        return SPACE_MATCHER;
    }

    public static StrMatcher splitMatcher() {
        return SPLIT_MATCHER;
    }

    public static StrMatcher trimMatcher() {
        return TRIM_MATCHER;
    }

    public static StrMatcher singleQuoteMatcher() {
        return SINGLE_QUOTE_MATCHER;
    }

    public static StrMatcher doubleQuoteMatcher() {
        return DOUBLE_QUOTE_MATCHER;
    }

    public static StrMatcher quoteMatcher() {
        return QUOTE_MATCHER;
    }

    public static StrMatcher noneMatcher() {
        return NONE_MATCHER;
    }

    public static StrMatcher charMatcher(char ch) {
        return new CharMatcher(ch);
    }

    public static StrMatcher charSetMatcher(char... chars) {
        if (chars == null || chars.length == 0) {
            return NONE_MATCHER;
        }
        if (chars.length == 1) {
            return new CharMatcher(chars[0]);
        }
        return new CharSetMatcher(chars);
    }

    public static StrMatcher charSetMatcher(String chars) {
        if (chars == null || chars.length() == 0) {
            return NONE_MATCHER;
        }
        if (chars.length() == 1) {
            return new CharMatcher(chars.charAt(0));
        }
        return new CharSetMatcher(chars.toCharArray());
    }

    public static StrMatcher stringMatcher(String str) {
        if (StringUtils.isEmpty(str)) {
            return NONE_MATCHER;
        }
        return new StringMatcher(str);
    }

    protected StrMatcher() {
        super();
    }

    public abstract int isMatch(char[] buffer, int pos, int bufferStart, int bufferEnd);

    public int isMatch(char[] buffer, int pos) {
        return isMatch(buffer, pos, 0, buffer.length);
    }

    static final class CharSetMatcher extends StrMatcher {
        private final char[] chars;

        CharSetMatcher(char chars[]) {
            super();
            this.chars = chars.clone();
            Arrays.sort(this.chars);
        }
        
        @Override
        public int isMatch(char[] buffer, int pos, int bufferStart, int bufferEnd) {
            return Arrays.binarySearch(chars, buffer[pos]) >= 0 ? 1 : 0;
        }
    }

    static final class CharMatcher extends StrMatcher {
        private final char ch;

        CharMatcher(char ch) {
            super();
            this.ch = ch;
        }

        @Override
        public int isMatch(char[] buffer, int pos, int bufferStart, int bufferEnd) {
            return ch == buffer[pos] ? 1 : 0;
        }
    }

    static final class StringMatcher extends StrMatcher {
        private final char[] chars;

        StringMatcher(String str) {
            super();
            chars = str.toCharArray();
        }

        @Override
        public int isMatch(char[] buffer, int pos, int bufferStart, int bufferEnd) {
            int len = chars.length;
            if (pos + len > bufferEnd) {
                return 0;
            }
            for (int i = 0; i < chars.length; i++, pos++) {
                if (chars[i] != buffer[pos]) {
                    return 0;
                }
            }
            return len;
        }
    }

    static final class NoMatcher extends StrMatcher {

        NoMatcher() {
            super();
        }

        @Override
        public int isMatch(char[] buffer, int pos, int bufferStart, int bufferEnd) {
            return 0;
        }
    }

    static final class TrimMatcher extends StrMatcher {

        TrimMatcher() {
            super();
        }

        @Override
        public int isMatch(char[] buffer, int pos, int bufferStart, int bufferEnd) {
            return buffer[pos] <= 32 ? 1 : 0;
        }
    }

}
