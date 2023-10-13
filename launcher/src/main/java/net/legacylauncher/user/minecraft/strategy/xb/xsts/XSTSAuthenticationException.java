package net.legacylauncher.user.minecraft.strategy.xb.xsts;

import net.legacylauncher.user.minecraft.strategy.MinecraftAuthenticationException;

public class XSTSAuthenticationException extends MinecraftAuthenticationException {
    public XSTSAuthenticationException() {
    }

    public XSTSAuthenticationException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getShortKey() {
        return "xsts_authentication";
    }
}
