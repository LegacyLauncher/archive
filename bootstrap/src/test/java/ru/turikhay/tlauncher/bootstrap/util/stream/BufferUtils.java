package ru.turikhay.tlauncher.bootstrap.util.stream;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

final class BufferUtils {
    static final String NEW_LINE = System.getProperty("line.separator");

    static String bufferToString(ByteArrayOutputStream buffer) {
        try {
            return buffer.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }
}
