package ru.turikhay.tlauncher.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.exceptions.AuthenticationException;
import org.apache.http.client.fluent.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.Lazy;
import ru.turikhay.util.async.AsyncThread;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class MojangUser extends MojangLikeUser {
    private static final Logger LOGGER = LogManager.getLogger(MojangUser.class);

    public static final String TYPE = "mojang";
    public static final MojangLikeUserFactory<MojangUser> FACTORY = MojangUser::new;

    MojangUser(AuthlibUserPayload payload) {
        super(payload);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    private final Lazy<CompletableFuture<MojangUserMigrationStatus>> migrateQueryJob = Lazy.of(() -> CompletableFuture.supplyAsync(() -> {
        try {
            getMojangUserAuthentication().logIn();
        } catch (AuthenticationException e) {
            LOGGER.warn("Couldn't log in {} before checking the migration status", getDisplayName(), e);
            return new MojangUserMigrationStatus(e);
        }
        JsonElement elem;
        try {
            elem = JsonParser.parseString(
                    Request.Get("https://api.minecraftservices.com/rollout/v1/msamigration")
                            .addHeader("Authorization", "Bearer " + getMojangUserAuthentication().getAuthenticatedToken())
                            .execute().returnContent().asString(StandardCharsets.UTF_8)
            );
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Couldn't fetch migration status for {}", getDisplayName(), e);
            return new MojangUserMigrationStatus(e);
        }
        boolean canMigrate = elem instanceof JsonObject
                && ((JsonObject) elem).has("rollout")
                && ((JsonObject) elem).getAsJsonPrimitive("rollout").getAsBoolean();
        LOGGER.debug("Can {} migrate? {}", getDisplayName(), canMigrate);
        return new MojangUserMigrationStatus(canMigrate);
    }, AsyncThread.SHARED_SERVICE));

    public Lazy<CompletableFuture<MojangUserMigrationStatus>> isReadyToMigrate() {
        return migrateQueryJob;
    }

    public MojangUserMigrationStatus.Status getMigrationStatusNow() {
        return migrateQueryJob.valueIfInitialized()
                .map(f -> f.getNow(null))
                .map(MojangUserMigrationStatus::asStatus)
                .orElse(MojangUserMigrationStatus.Status.NONE);
    }

    public static MojangLikeUserJsonizer<MojangUser> getJsonizer() {
        return new MojangLikeUserJsonizer<>(new MojangAuth(), FACTORY);
    }
}
