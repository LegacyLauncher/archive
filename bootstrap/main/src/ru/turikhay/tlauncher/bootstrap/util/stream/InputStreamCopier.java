package ru.turikhay.tlauncher.bootstrap.util.stream;

import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InputStreamCopier extends Thread {

    private final InputStream input;
    private final OutputStream output;

    public InputStreamCopier(InputStream inputStream, OutputStream outputStream) {
        this.input = U.requireNotNull(inputStream, "input");
        this.output = U.requireNotNull(outputStream, "output");
    }

    public void run() {
        try {
            byte[] buffer = new byte[8192];
            int read;
            while((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
                if(Thread.interrupted()) {
                    return;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
