package net.legacylauncher.user.minecraft.strategy.mcsauth;

import net.legacylauncher.user.minecraft.strategy.MinecraftAuthenticationException;

public class MinecraftServicesAuthenticationException extends MinecraftAuthenticationException {
    public MinecraftServicesAuthenticationException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getShortKey() {
        return "minecraft_services_auth";
    }
}
