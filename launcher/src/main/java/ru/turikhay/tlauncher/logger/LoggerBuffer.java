package ru.turikhay.tlauncher.logger;

@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
public class LoggerBuffer implements LoggerInterface {
    private final int capacity;
    private StringBuilder buffer;

    public LoggerBuffer() {
        this(16384);
    }

    LoggerBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = new StringBuilder(capacity);
    }

    @Override
    public void print(String message) {
        final StringBuilder buffer = this.buffer;
        if (buffer != null) {
            synchronized (buffer) {
                if (buffer.length() + message.length() >= capacity) {
                    buffer.setLength(0);
                }
                buffer.append(message);
            }
        }
    }

    public String drain() {
        final StringBuilder buffer = this.buffer;
        if (buffer == null) {
            return null;
        }
        synchronized (buffer) {
            try {
                return buffer.toString();
            } finally {
                cleanup();
            }
        }
    }

    public void cleanup() {
        if (buffer != null) {
            buffer = null;
        }
    }
}
