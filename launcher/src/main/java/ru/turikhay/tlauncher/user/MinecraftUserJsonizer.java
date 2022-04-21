package ru.turikhay.tlauncher.user;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import ru.turikhay.tlauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import ru.turikhay.tlauncher.user.minecraft.strategy.oatoken.MicrosoftOAuthToken;
import ru.turikhay.tlauncher.user.minecraft.strategy.preq.MinecraftOAuthProfile;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class MinecraftUserJsonizer extends UserJsonizer<MinecraftUser> {
    @Override
    public JsonObject serialize(MinecraftUser src, JsonSerializationContext context) {
        return (JsonObject) context.serialize(new Payload(src));
    }

    @Override
    public MinecraftUser deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        return ((Payload) context.deserialize(json, Payload.class)).toUser();
    }

    private static class Payload {
        final UUID id;
        final String name;

        final MinecraftTokenPayload minecraft;
        final MicrosoftTokenPayload microsoft;

        Payload(MinecraftUser src) {
            this.id = src.getUUID();
            this.name = src.getUsername();
            this.minecraft = new MinecraftTokenPayload(src.getMinecraftToken());
            this.microsoft = new MicrosoftTokenPayload(src.getMicrosoftToken());
        }

        MinecraftUser toUser() {
            return new MinecraftUser(
                    new MinecraftOAuthProfile(
                            Objects.requireNonNull(id, "id"),
                            Objects.requireNonNull(name, "name")
                    ),
                    Objects.requireNonNull(microsoft, "microsoftToken").toToken(),
                    Objects.requireNonNull(minecraft, "minecraftToken").toToken()
            );
        }
    }

    private static class MinecraftTokenPayload {
        final String token;
        final Instant expiresAt;

        MinecraftTokenPayload(MinecraftServicesToken token) {
            this.token = token.getAccessToken();
            this.expiresAt = token.calculateExpiryTime();
        }

        MinecraftServicesToken toToken() {
            return new MinecraftServicesToken(
                    Objects.requireNonNull(token, "token"),
                    Objects.requireNonNull(expiresAt, "expiresAt")
            );
        }
    }

    private static class MicrosoftTokenPayload {
        final String accessToken;
        final String refreshToken;
        final Instant expiresAt;

        MicrosoftTokenPayload(MicrosoftOAuthToken token) {
            this.accessToken = token.getAccessToken();
            this.refreshToken = token.getRefreshToken();
            this.expiresAt = token.calculateExpiryTime();
        }

        MicrosoftOAuthToken toToken() {
            return new MicrosoftOAuthToken(
                    Objects.requireNonNull(accessToken, "accessToken"),
                    Objects.requireNonNull(refreshToken, "refreshToken"),
                    Objects.requireNonNull(expiresAt, "expiresAt")
            );
        }
    }
}
