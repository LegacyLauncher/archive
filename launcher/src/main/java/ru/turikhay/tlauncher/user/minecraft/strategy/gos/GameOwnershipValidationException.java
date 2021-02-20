package ru.turikhay.tlauncher.user.minecraft.strategy.gos;

import ru.turikhay.tlauncher.user.minecraft.strategy.MinecraftAuthenticationException;

public class GameOwnershipValidationException extends MinecraftAuthenticationException {
    private final boolean isKnownNotToOwn;

    public GameOwnershipValidationException(Throwable cause) {
        super(cause);
        this.isKnownNotToOwn = false; // was caused by another issue
    }

    public GameOwnershipValidationException(String message) {
        super(message);
        this.isKnownNotToOwn = true;
    }

    public boolean isKnownNotToOwn() {
        return isKnownNotToOwn;
    }

    @Override
    public String getShortKey() {
        return "game_ownership";
    }
}
