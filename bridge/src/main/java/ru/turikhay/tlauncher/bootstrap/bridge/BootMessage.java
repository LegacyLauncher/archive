package ru.turikhay.tlauncher.bootstrap.bridge;

public final class BootMessage {
    private final String title;
    private final String message;

    BootMessage(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return message;
    }
}
