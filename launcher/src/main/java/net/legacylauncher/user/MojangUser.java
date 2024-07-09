package net.legacylauncher.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.exceptions.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.util.Lazy;
import net.legacylauncher.util.async.AsyncThread;
import org.apache.hc.client5.http.fluent.Request;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MojangUser extends MojangLikeUser {
    public static final String TYPE = "mojang";
    public static final MojangLikeUserFactory<MojangUser> FACTORY = MojangUser::new;
    private final Lazy<CompletableFuture<MojangUserMigrationStatus>> migrateQueryJob = Lazy.of(() -> CompletableFuture.supplyAsync(() -> {
        try {
            getMojangUserAuthentication().logIn();
        } catch (AuthenticationException e) {
            log.warn("Couldn't log in {} before checking the migration status", getDisplayName(), e);
            return new MojangUserMigrationStatus(e);
        }
        JsonElement elem;
        try {
            elem = JsonParser.parseString(
                    Request.get("https://api.minecraftservices.com/rollout/v1/msamigration")
                            .addHeader("Authorization", "Bearer " + getMojangUserAuthentication().getAuthenticatedToken())
                            .execute().returnContent().asString(StandardCharsets.UTF_8)
            );
        } catch (IOException | RuntimeException e) {
            log.warn("Couldn't fetch migration status for {}", getDisplayName(), e);
            return new MojangUserMigrationStatus(e);
        }
        boolean canMigrate = elem instanceof JsonObject
                && ((JsonObject) elem).has("rollout")
                && ((JsonObject) elem).getAsJsonPrimitive("rollout").getAsBoolean();
        log.debug("Can {} migrate? {}", getDisplayName(), canMigrate);
        return new MojangUserMigrationStatus(canMigrate);
    }, AsyncThread.SHARED_SERVICE));

    MojangUser(AuthlibUserPayload payload) {
        super(payload);
    }

    public static MojangLikeUserJsonizer<MojangUser> getJsonizer() {
        return new MojangLikeUserJsonizer<>(new MojangAuth(), FACTORY);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public Lazy<CompletableFuture<MojangUserMigrationStatus>> isReadyToMigrate() {
        return migrateQueryJob;
    }

    public MojangUserMigrationStatus.Status getMigrationStatusNow() {
        return migrateQueryJob.valueIfInitialized()
                .map(f -> f.getNow(null))
                .map(MojangUserMigrationStatus::asStatus)
                .orElse(MojangUserMigrationStatus.Status.NONE);
    }
}
