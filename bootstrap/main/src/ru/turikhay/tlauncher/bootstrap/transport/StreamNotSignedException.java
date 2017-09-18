package ru.turikhay.tlauncher.bootstrap.transport;

import java.io.IOException;

public class StreamNotSignedException extends IOException {
    public StreamNotSignedException(String message) {
        super(message);
    }
}
