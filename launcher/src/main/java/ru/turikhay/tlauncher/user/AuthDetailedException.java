package ru.turikhay.tlauncher.user;

public class AuthDetailedException extends AuthException {
    private final String content;

    public AuthDetailedException(String content) {
        super(content, "detailed");
        this.content = content;
    }

    public String getErrorContent() {
        return content;
    }
}
