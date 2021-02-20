package ru.turikhay.tlauncher.user.minecraft.strategy.pconv;

import ru.turikhay.tlauncher.user.minecraft.strategy.MinecraftAuthenticationException;

public class MinecraftProfileConversionException extends MinecraftAuthenticationException {
    public MinecraftProfileConversionException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getShortKey() {
        return "minecraft_profile_conversion";
    }
}
