package ru.turikhay.util.stream;

import java.io.OutputStream;

public abstract class SafeOutputStream extends OutputStream {
    public void write(byte[] b) {
    }

    public void write(byte[] b, int off, int len) {
    }

    public void flush() {
    }

    public void close() {
    }

    public abstract void write(int var1);
}
