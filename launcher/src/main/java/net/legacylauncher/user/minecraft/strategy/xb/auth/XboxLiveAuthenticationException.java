package net.legacylauncher.user.minecraft.strategy.xb.auth;

import net.legacylauncher.user.minecraft.strategy.MinecraftAuthenticationException;

public class XboxLiveAuthenticationException extends MinecraftAuthenticationException {
    public XboxLiveAuthenticationException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getShortKey() {
        return "xbox_live_authentication";
    }
}
