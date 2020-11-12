package ru.turikhay.tlauncher.pasta;

import java.io.IOException;

public class TooManyRequests extends IOException {

    public TooManyRequests() {
    }

    public TooManyRequests(Throwable cause) {
        super(cause);
    }
}
