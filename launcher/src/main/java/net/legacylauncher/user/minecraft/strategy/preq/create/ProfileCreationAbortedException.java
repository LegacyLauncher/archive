package net.legacylauncher.user.minecraft.strategy.preq.create;

import net.legacylauncher.user.minecraft.strategy.MinecraftAuthenticationException;

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
