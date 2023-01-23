package ru.turikhay.tlauncher.user;

import ru.turikhay.tlauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import ru.turikhay.tlauncher.user.minecraft.strategy.oatoken.MicrosoftOAuthToken;
import ru.turikhay.tlauncher.user.minecraft.strategy.preq.MinecraftOAuthProfile;

import java.util.Objects;
import java.util.UUID;

public class MinecraftUser extends User {
    public static final String TYPE = "minecraft";

    private MinecraftOAuthProfile profile;
    private MicrosoftOAuthToken microsoftToken;
    private MinecraftServicesToken minecraftToken;

    public MinecraftUser(MinecraftOAuthProfile profile,
                         MicrosoftOAuthToken microsoftToken,
                         MinecraftServicesToken minecraftToken) {
        setPayload(profile, microsoftToken, minecraftToken);
    }

    @Override
    public String getUsername() {
        return profile.getName();
    }

    @Override
    public String getDisplayName() {
        return profile.getName();
    }

    @Override
    public UUID getUUID() {
        return profile.getId();
    }

    public MicrosoftOAuthToken getMicrosoftToken() {
        return microsoftToken;
    }

    public MinecraftServicesToken getMinecraftToken() {
        return minecraftToken;
    }

    void setPayload(MinecraftOAuthProfile profile, MicrosoftOAuthToken microsoftToken, MinecraftServicesToken minecraftToken) {
        setProfile(profile);
        setMicrosoftToken(microsoftToken);
        setMinecraftToken(minecraftToken);
    }

    public void setProfile(MinecraftOAuthProfile profile) {
        this.profile = Objects.requireNonNull(profile, "profile");
    }

    void setMicrosoftToken(MicrosoftOAuthToken microsoftToken) {
        this.microsoftToken = Objects.requireNonNull(microsoftToken, "microsoftToken");
    }

    void setMinecraftToken(MinecraftServicesToken minecraftToken) {
        this.minecraftToken = Objects.requireNonNull(minecraftToken, "minecraftToken");
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    protected boolean equals(User user) {
        return user != null && profile.getId().equals(((MinecraftUser) user).profile.getId());
    }

    @Override
    public int hashCode() {
        int result = profile.hashCode();
        result = 31 * result + microsoftToken.hashCode();
        result = 31 * result + minecraftToken.hashCode();
        return result;
    }

    @Override
    public LoginCredentials getLoginCredentials() {
        return new LoginCredentials(
                profile.getName(),
                minecraftToken.getAccessToken(),
                "{}",
                profile.getName(),
                profile.getId(),
                "mojang",
                profile.getName()
        );
    }

    public static MinecraftUserJsonizer getJsonizer() {
        return new MinecraftUserJsonizer();
    }
}
