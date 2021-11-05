package ru.turikhay.tlauncher.user.minecraft.strategy.preq.create;

import ru.turikhay.tlauncher.user.minecraft.strategy.MinecraftAuthenticationException;

public class ProfileCreationAbortedException extends MinecraftAuthenticationException {
    public ProfileCreationAbortedException() {
    }

    public ProfileCreationAbortedException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getShortKey() {
        return "minecraft_profile_creation_aborted";
    }
}
