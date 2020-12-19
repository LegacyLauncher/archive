package ru.turikhay.tlauncher.pasta;

import java.util.Locale;

public enum PastaFormat {
    LOGS,
    JSON;

    String value() {
        return name().toLowerCase(Locale.ROOT);
    }
}
