package ru.turikhay.util.stream;

import ru.turikhay.util.U;

import java.io.IOException;
import java.io.InputStream;

public class InputStringStream extends InputStream {
    private final String s;
    private int i = -1;

    public InputStringStream(String s) {
        if (s == null)
            throw new NullPointerException();

        this.s = s;
    }

    @Override
    public int available() {
        return (i == -1) ? s.length() : s.length() - i - 1;
    }

    @Override
    public int read() {
        if (++i >= s.length())
            return -1;
        return s.charAt(i);
    }
}
