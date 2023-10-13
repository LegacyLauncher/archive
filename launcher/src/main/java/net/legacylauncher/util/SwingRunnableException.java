package net.legacylauncher.util;

class SwingRunnableException extends RuntimeException {
    public SwingRunnableException(Throwable cause) {
        super(cause.toString(), cause, false, false);
    }
}
