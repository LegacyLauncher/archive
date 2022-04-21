package ru.turikhay.tlauncher.bootstrap.util.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class ByteArrayBufferOutputStream extends ToStringBufferOutputStream {
    private static final int DEFAULT_MAX_COUNT = 4 * 1024 * 1024;

    private volatile ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final Object lock = new Object();
    private final int maxCount;

    ByteArrayBufferOutputStream(int maxCount) {
        this.maxCount = maxCount;
    }

    ByteArrayBufferOutputStream() {
        this(DEFAULT_MAX_COUNT);
    }

    public String getBuffer() {
        synchronized (lock) {
            return buffer.toString();
        }
    }

    public void clearBuffer() {
        synchronized (lock) {
            buffer.reset();
            buffer = new ByteArrayOutputStream();
        }
    }

    @Override
    public void write(int b) {
        synchronized (lock) {
            if (buffer.size() < maxCount) {
                buffer.write(b);
            }
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        synchronized (lock) {
            if (buffer.size() < maxCount) {
                buffer.write(b);
            }
        }
    }

    @Override
    public void write(byte[] b, int off, int len) {
        synchronized (lock) {
            if (buffer.size() < maxCount) {
                buffer.write(b, off, len);
            }
        }
    }

    ByteArrayOutputStream buffer() {
        return buffer;
    }
}
