package net.legacylauncher.user.minecraft.strategy.oareq;

import java.net.URI;
import java.util.Objects;

public class MicrosoftOAuthExchangeCode {
    private final String code;
    private final URI redirectUrl;

    public MicrosoftOAuthExchangeCode(String code, URI redirectUrl) {
        this.code = Objects.requireNonNull(code, "code");
        this.redirectUrl = Objects.requireNonNull(redirectUrl, "redirectUrl");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MicrosoftOAuthExchangeCode that = (MicrosoftOAuthExchangeCode) o;

        if (!code.equals(that.code)) return false;
        return redirectUrl.equals(that.redirectUrl);
    }

    @Override
    public int hashCode() {
        int result = code.hashCode();
        result = 31 * result + redirectUrl.hashCode();
        return result;
    }

    public String getCode() {
        return code;
    }

    public URI getRedirectUrl() {
        return redirectUrl;
    }

    @Override
    public String toString() {
        return "MicrosoftOAuthExchangeCode{" +
                "code='" + code + '\'' +
                ", redirectUrl='" + redirectUrl + '\'' +
                '}';
    }
}
