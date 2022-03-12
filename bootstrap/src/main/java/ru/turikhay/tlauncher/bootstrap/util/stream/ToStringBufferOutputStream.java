package ru.turikhay.tlauncher.bootstrap.util.stream;

import java.io.OutputStream;

public abstract class ToStringBufferOutputStream extends OutputStream {
    public abstract String getBuffer();

    public abstract void clearBuffer();
}
