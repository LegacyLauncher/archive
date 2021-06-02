package ru.turikhay.tlauncher.user.minecraft.strategy.mcsauth;

import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.Validatable;

import java.time.Instant;

import static ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.Validatable.notEmpty;
import static ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.Validatable.notNegative;

public class MinecraftServicesToken implements Validatable {
    private String accessToken;
    private int expiresIn;

    private final transient Instant createdAt;

    public MinecraftServicesToken(String accessToken, int expiresIn) {
        this.accessToken = accessToken;
        this.createdAt = Instant.now();
        this.expiresIn = expiresIn;
    }

    public MinecraftServicesToken(String accessToken, Instant expiresAt) {
        this.accessToken = accessToken;
        // let's make a guess :)
        this.createdAt = expiresAt.minusSeconds(3600);
        this.expiresIn = 3600;
    }

    public MinecraftServicesToken() {
        this.createdAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MinecraftServicesToken that = (MinecraftServicesToken) o;

        if (expiresIn != that.expiresIn) return false;
        return accessToken.equals(that.accessToken);
    }

    @Override
    public int hashCode() {
        int result = accessToken.hashCode();
        result = 31 * result + expiresIn;
        return result;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public Instant calculateExpiryTime() {
        return createdAt.plusSeconds(expiresIn);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(calculateExpiryTime());
    }

    @Override
    public String toString() {
        return "MinecraftServicesToken{" +
                "accessToken='" + accessToken + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }

    @Override
    public void validate() {
        notEmpty(accessToken, "accessToken");
        notNegative(expiresIn, "expiresIn");
    }
}
