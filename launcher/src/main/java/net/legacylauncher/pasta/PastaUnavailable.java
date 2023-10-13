package net.legacylauncher.pasta;

import java.io.IOException;

public class PastaUnavailable extends IOException {
    public PastaUnavailable(int statusCode, String message) {
        super(statusCode + ": " + message);
    }
}
