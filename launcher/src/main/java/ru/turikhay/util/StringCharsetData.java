package ru.turikhay.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class StringCharsetData implements CharsetData {
    private static final int PRETTY_LONG_STRING = 1024 * 512;
    private static final int SIZE_UNKNOWN = -1, SIZE_TOO_LONG = Integer.MIN_VALUE;

    private final String string;
    private int size = -2;

    public StringCharsetData(String string) {
        this.string = Objects.requireNonNull(string, "string");
    }

    @Override
    public StringReader read() throws IOException {
        return new StringReader(string);
    }

    @Override
    public InputStream stream() throws IOException {
        return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Charset charset() {
        return StandardCharsets.UTF_8;
    }

    @Override
    public long length() {
        if(size == SIZE_UNKNOWN) {
            if(string.length() > PRETTY_LONG_STRING) {
                size = SIZE_TOO_LONG;
            } else {
                size = string.getBytes(StandardCharsets.UTF_8).length;
            }
        }
        return size == SIZE_TOO_LONG ? SIZE_UNKNOWN : size;
    }
}
