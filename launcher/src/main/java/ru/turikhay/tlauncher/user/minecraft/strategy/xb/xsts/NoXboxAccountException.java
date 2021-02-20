package ru.turikhay.tlauncher.user.minecraft.strategy.xb.xsts;

public class NoXboxAccountException extends XSTSAuthenticationException {
    @Override
    public String getShortKey() {
        return super.getShortKey() + ".no_xbox_account";
    }
}
