package ru.turikhay.tlauncher.user.minecraft.strategy.mcsauth;

import ru.turikhay.tlauncher.user.minecraft.strategy.MinecraftAuthenticationException;

public class MinecraftServicesAuthenticationException extends MinecraftAuthenticationException {
    public MinecraftServicesAuthenticationException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getShortKey() {
        return "minecraft_services_auth";
    }
}
