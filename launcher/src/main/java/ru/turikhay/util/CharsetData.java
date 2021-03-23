package ru.turikhay.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

public interface CharsetData {
    Reader read() throws IOException;

    InputStream stream() throws IOException;

    Charset charset();

    /**
     * Requests data length
     * @return data length in bytes, or {@code -1} if unknown.
     */
    long length();
}
