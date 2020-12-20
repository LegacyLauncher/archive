package ru.turikhay.util;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class StringCharsetData implements CharsetData {
    private final String string;
    private int size = -1;

    public StringCharsetData(String string) {
        this.string = Objects.requireNonNull(string, "string");
    }

    @Override
    public StringReader read() throws IOException {
        return new StringReader(string);
    }

    @Override
    public long length() {
        if(size == -1) {
            size = string.getBytes(StandardCharsets.UTF_8).length;
        }
        return size;
    }
}
