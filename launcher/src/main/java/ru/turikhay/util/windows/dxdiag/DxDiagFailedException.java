package ru.turikhay.util.windows.dxdiag;

public class DxDiagFailedException extends Exception {
    public DxDiagFailedException(String message) {
        super(message);
    }

    public DxDiagFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
