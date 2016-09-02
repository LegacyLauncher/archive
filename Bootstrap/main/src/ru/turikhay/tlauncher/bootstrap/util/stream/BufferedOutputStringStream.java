package ru.turikhay.tlauncher.bootstrap.util.stream;

import org.apache.commons.io.IOUtils;

import java.io.*;

public class BufferedOutputStringStream extends OutputStream {
    private final ByteBuffer bBuffer = new ByteBuffer();
    protected String bufferedLine;

    protected int caret;

    public synchronized void write(int b) {
        bBuffer.write(b);

        if(b == '\n') {
            try {
                bufferedLine = new String(IOUtils.toByteArray(bBuffer));
            } catch(IOException ioE) {
                // what the heck?
                throw new Error(ioE);
            }
            flush();
        }
    }

    public synchronized void flush() {
        caret = 0;
        bufferedLine = "";
    }

    private class ByteBuffer extends InputStream {
        private byte[] buffer = new byte[1024];
        private int readIndex = 0, writeIndex = 0;

        public void write(int b) {
            if(writeIndex == buffer.length) {
                // need to grow
                byte[] newBuffer = new byte[buffer.length + 1024];
                System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
                buffer = newBuffer;
            }
            buffer[writeIndex++] = (byte) b;
        }

        @Override
        public int read() {
            if(writeIndex == -1 || readIndex == writeIndex) {
                return -1;
            }
            return buffer[readIndex++];
        }

        public void reset() {
            readIndex = 0;
            writeIndex = 0;
        }
    }
}
