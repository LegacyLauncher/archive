package ru.turikhay.tlauncher.user.minecraft.strategy.xb.xsts;

public class CountryNotAuthorizedException extends XSTSAuthenticationException {
    @Override
    public String getShortKey() {
        return super.getShortKey() + ".country_not_authorized";
    }
}
