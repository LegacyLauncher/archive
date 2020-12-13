package ru.turikhay.tlauncher.bootstrap.pasta;

import java.io.IOException;

public class TooManyRequests extends IOException {
    public static final int RESPONSE_CODE = 429;

    TooManyRequests(Throwable cause) {
        super(cause);
    }
}
