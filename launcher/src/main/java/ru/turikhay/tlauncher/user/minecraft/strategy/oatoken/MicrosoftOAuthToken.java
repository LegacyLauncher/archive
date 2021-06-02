package ru.turikhay.tlauncher.user.minecraft.strategy.oatoken;

import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.Validatable;

import java.time.Instant;
import java.util.Objects;

import static ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.Validatable.notNegative;
import static ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.Validatable.notNull;

public class MicrosoftOAuthToken implements Validatable {
    private String accessToken;
    private String refreshToken;
    private int expiresIn;

    private final transient Instant createdAt;

    public MicrosoftOAuthToken(String accessToken, String refreshToken, int expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.createdAt = Instant.now();
        this.expiresIn = expiresIn;
    }

    public MicrosoftOAuthToken(String accessToken, String refreshToken, Instant expiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        // let's make a guess :)
        this.createdAt = expiresAt.minusSeconds(3600);
        this.expiresIn = 3600;
    }

    public MicrosoftOAuthToken() {
        this.createdAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MicrosoftOAuthToken that = (MicrosoftOAuthToken) o;

        if (expiresIn != that.expiresIn) return false;
        if (!Objects.equals(accessToken, that.accessToken)) return false;
        return Objects.equals(refreshToken, that.refreshToken);
    }

    @Override
    public int hashCode() {
        int result = accessToken != null ? accessToken.hashCode() : 0;
        result = 31 * result + (refreshToken != null ? refreshToken.hashCode() : 0);
        result = 31 * result + expiresIn;
        return result;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Instant calculateExpiryTime() {
        return createdAt.plusSeconds(expiresIn);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(calculateExpiryTime());
    }

    @Override
    public String toString() {
        return "MicrosoftOAuthToken{" +
                "accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", expiresIn=" + expiresIn +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public void validate() {
        notNull(accessToken, "accessToken");
        notNull(refreshToken, "refreshToken");
        notNegative(expiresIn, "refreshToken");
        notNull(createdAt, "createdAt");
    }
}
