package ru.turikhay.tlauncher.exceptions;

import ru.turikhay.util.OS;
import ru.turikhay.util.U;

public class UnsupportedEnvirnomentException extends Error {
    public UnsupportedEnvirnomentException(String message) {
        super(message);
    }

    public static void ensureUnder(OS os) throws UnsupportedEnvirnomentException {
        if (OS.CURRENT != U.requireNotNull(os, "os")) {
            throw new UnsupportedEnvirnomentException(os + " is required to perform this action");
        }
    }
}
