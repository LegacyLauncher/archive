package net.legacylauncher.user.minecraft.strategy.preq;

import net.legacylauncher.user.minecraft.strategy.MinecraftAuthenticationException;

public class MinecraftProfileRequestException extends MinecraftAuthenticationException {
    public MinecraftProfileRequestException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getShortKey() {
        return "minecraft_profile_request";
    }
}
