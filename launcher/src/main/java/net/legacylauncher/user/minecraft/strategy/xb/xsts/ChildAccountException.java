package net.legacylauncher.user.minecraft.strategy.xb.xsts;

public class ChildAccountException extends XSTSAuthenticationException {
    @Override
    public String getShortKey() {
        return super.getShortKey() + ".child_account";
    }
}
