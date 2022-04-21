package ru.turikhay.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

public interface CharsetData {
    int UNKNOWN_LENGTH = -1;

    Reader read() throws IOException;

    InputStream stream() throws IOException;

    Charset charset();

    /**
     * Requests data length
     *
     * @return data length in bytes, or {@link #UNKNOWN_LENGTH}
     */
    long length();
}
