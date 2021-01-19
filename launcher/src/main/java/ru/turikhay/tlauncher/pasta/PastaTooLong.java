package ru.turikhay.tlauncher.pasta;

import java.io.IOException;

public class PastaTooLong extends IOException {
    public PastaTooLong(long length) {
        super(String.valueOf(length));
    }
}
