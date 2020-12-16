package ru.turikhay.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.Objects;

public class StringCharsetData implements CharsetData {
    private final String string;

    public StringCharsetData(String string) {
        this.string = Objects.requireNonNull(string, "string");
    }

    @Override
    public StringReader read() throws IOException {
        return new StringReader(string);
    }
}
