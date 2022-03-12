package ru.turikhay.tlauncher.bootstrap.util.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

class RedirectOutputStream extends OutputStream {
    private final OutputStream buffer;
    private final PrintStream targetStream;

    private boolean enableRecording = true;

    public RedirectOutputStream(OutputStream buffer, PrintStream targetStream) {
        this.buffer = buffer;
        this.targetStream = targetStream;
    }

    public void disableRecording() {
        this.enableRecording = false;
    }

    @Override
    public synchronized void write(int b) throws IOException {
        if (enableRecording) {
            buffer.write(b);
        }
        targetStream.write(b);
    }

    @Override
    public synchronized void write(byte[] b) throws IOException {
        if (enableRecording) {
            buffer.write(b);
        }
        targetStream.write(b, 0, b.length);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        if (enableRecording) {
            buffer.write(b, off, len);
        }
        targetStream.write(b, off, len);
    }

    public synchronized void flush() {
        targetStream.flush();
    }

    @Override
    public void close() {
        targetStream.close();
    }
}
