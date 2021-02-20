package ru.turikhay.tlauncher.user.minecraft.strategy;

public abstract class MinecraftAuthenticationException extends Exception {
    public MinecraftAuthenticationException() {
    }

    public MinecraftAuthenticationException(String message) {
        super(message);
    }

    public MinecraftAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    public MinecraftAuthenticationException(Throwable cause) {
        super(cause);
    }

    public abstract String getShortKey();
}
