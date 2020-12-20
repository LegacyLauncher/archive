package ru.turikhay.util;

import java.io.IOException;
import java.io.Reader;

public interface CharsetData {
    Reader read() throws IOException;

    /**
     * Requests data length
     * @return data length in bytes, or {@code -1} if unknown.
     */
    long length();
}
