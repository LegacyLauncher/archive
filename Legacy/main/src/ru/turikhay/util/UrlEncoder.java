package ru.turikhay.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.BitSet;

public class UrlEncoder {
    private static BitSet dontNeedEncoding;
    private static final int caseDiff = ('a' - 'A');

    static {
        /* The list of characters that are not encoded has been
         * determined as follows:
         *
         * RFC 2396 states:
         * -----
         * Data characters that are allowed in a URI but do not have a
         * reserved purpose are called unreserved.  These include upper
         * and lower case letters, decimal digits, and a limited set of
         * punctuation marks and symbols.
         *
         * unreserved  = alphanum | mark
         *
         * mark        = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
         *
         * Unreserved characters can be escaped without changing the
         * semantics of the URI, but this should not be done unless the
         * URI is being used in a context that does not allow the
         * unescaped character to appear.
         * -----
         *
         * It appears that both Netscape and Internet Explorer escape
         * all special characters from this list with the exception
         * of "-", "_", ".", "*". While it is not clear why they are
         * escaping the other characters, perhaps it is safest to
         * assume that there might be contexts in which the others
         * are unsafe if not escaped. Therefore, we will use the same
         * list. It is also noteworthy that this is consistent with
         * O'Reilly's "HTML: The Definitive Guide" (page 164).
         *
         * As a last note, Intenet Explorer does not encode the "@"
         * character which is clearly not unreserved according to the
         * RFC. We are being consistent with the RFC in this matter,
         * as is Netscape.
         */


        dontNeedEncoding = new BitSet(256);
        int i;
        for (i = 'a'; i <= 'z'; i++) {
            dontNeedEncoding.set(i);
        }
        for (i = 'A'; i <= 'Z'; i++) {
            dontNeedEncoding.set(i);
        }
        for (i = '0'; i <= '9'; i++) {
            dontNeedEncoding.set(i);
        }
        dontNeedEncoding.set(' '); /* encoding a space to a + is done
                                    * in the encode() method */
        dontNeedEncoding.set('-');
        dontNeedEncoding.set('_');
        dontNeedEncoding.set('.');
        dontNeedEncoding.set('*');
    }

    private final CharSequence s;

    public UrlEncoder(CharSequence sequence) {
        s = U.requireNotNull(sequence);
    }

    public Encoder getEncoder() {
        return new Encoder();
    }

    public String encode() {
        try {
            return IOUtils.toString(new Encoder(), FileUtil.DEFAULT_CHARSET);
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    private class CharBuffer {
        private char buf[];
        protected int start, count;

        CharBuffer(int size) {
            buf = new char[size];
        }

        public void append(int c) {
            int newcount = count + 1;
            if (newcount > buf.length) {
                buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
            }
            buf[count] = (char) c;
            count = newcount;
        }

        public boolean hasFlush() {
            if (start < count) {
                return true;
            }
            reset();
            return false;
        }

        public int flush() {
            return start == count ? -1 : buf[start++];
        }

        public void reset() {
            start = 0;
            count = 0;
        }

        public String toString() {
            return new String(buf, start, count);
        }
    }

    public class Encoder extends InputStream {
        CharBuffer outputBuffer = new CharBuffer(4), surrogateBuffer = new CharBuffer(2);
        int i = -1;

        @Override
        public int read() throws IOException {
            if (surrogateBuffer.hasFlush()) {
                String str = surrogateBuffer.toString();
                byte[] ba = str.getBytes(FileUtil.DEFAULT_CHARSET);
                for (int j = 0; j < ba.length; j++) {
                    outputBuffer.append('%');
                    char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
                    // converting to use uppercase letter as part of
                    // the hex value if ch is a letter.
                    if (Character.isLetter(ch)) {
                        ch -= caseDiff;
                    }
                    outputBuffer.append(ch);
                    ch = Character.forDigit(ba[j] & 0xF, 16);
                    if (Character.isLetter(ch)) {
                        ch -= caseDiff;
                    }
                    outputBuffer.append(ch);
                }
                surrogateBuffer.reset();
            }

            if (outputBuffer.hasFlush()) {
                return outputBuffer.flush();
            }

            if (++i >= s.length()) {
                return -1;
            }

            int c = (int) s.charAt(i);

            if (dontNeedEncoding.get(c)) {
                if (c == ' ') {
                    c = '+';
                }
                return c;
            } else {
                // convert to external encoding before hex conversion
                do {
                    surrogateBuffer.append(c);
                    /*
                     * If this character represents the start of a Unicode
                     * surrogate pair, then pass in two characters. It's not
                     * clear what should be done if a bytes reserved in the
                     * surrogate pairs range occurs outside of a legal
                     * surrogate pair. For now, just treat it as if it were
                     * any other character.
                     */
                    if (c >= 0xD800 && c <= 0xDBFF) {
                        /*
                          System.out.println(Integer.toHexString(c)
                          + " is high surrogate");
                        */
                        if ((i + 1) < s.length()) {
                            int d = (int) s.charAt(i + 1);
                            /*
                              System.out.println("\tExamining "
                              + Integer.toHexString(d));
                            */
                            if (d >= 0xDC00 && d <= 0xDFFF) {
                                /*
                                  System.out.println("\t"
                                  + Integer.toHexString(d)
                                  + " is low surrogate");
                                */
                                surrogateBuffer.append(d);
                                i++;
                            }
                        }
                    }
                    i++;
                } while (i < s.length() && !dontNeedEncoding.get((c = (int) s.charAt(i))));

                i--;

                return read();
            }
        }
    }

    public static String encode(String str) {
        return new UrlEncoder(str).encode();
    }
}
