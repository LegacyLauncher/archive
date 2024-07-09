package net.legacylauncher.bootstrap.meta;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.Value;
import net.legacylauncher.bootstrap.launcher.Library;

import java.lang.reflect.Type;
import java.util.*;

@Value
@AllArgsConstructor
public class LocalLauncherMeta implements LauncherMeta {
    Version version;
    String shortBrand;
    String brand;
    List<Library> libraries;
    Map<EntrypointType, Entrypoint> entrypoints;

    public LocalLauncherMeta(OldLauncherMeta old) {
        this.shortBrand = old.getShortBrand();
        this.version = old.getVersion();
        this.brand = old.getBrand();
        this.entrypoints = new HashMap<>();
        entrypoints.put(EntrypointType.Bridge, new Entrypoint(old.getMainClass(), "launch"));
        this.libraries = old.getLibraries();
    }

    public Map<EntrypointType, Entrypoint> getEntrypoints() {
        return Collections.unmodifiableMap(entrypoints);
    }

    public List<Library> getLibraries() {
        return Collections.unmodifiableList(libraries);
    }

    public Entrypoint getEntrypoint(EntrypointType type) {
        Entrypoint entrypoint = entrypoints.get(type);
        if (entrypoint == null) {
            throw new NullPointerException("Couldn't find entrypoint of type " + type);
        }
        return entrypoint;
    }

    public boolean hasEntrypoint(EntrypointType type) {
        return entrypoints.containsKey(type);
    }

    public enum EntrypointType {
        @SerializedName("bridge")
        Bridge,
        @SerializedName("dbusP2P")
        DBusP2P,
        @SerializedName("dbusSession")
        DBusSession,
    }

    @Value
    public static class Entrypoint {
        String type;
        String method;
    }

    public static class Deserializer implements JsonDeserializer<LocalLauncherMeta> {
        @Override
        public LocalLauncherMeta deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            Version version = context.deserialize(object.getAsJsonPrimitive("version"), Version.class);
            String shortBrand = object.getAsJsonPrimitive("shortBrand").getAsString();
            String brand = object.getAsJsonPrimitive("brand").getAsString();
            List<Library> libraries = context.deserialize(object.getAsJsonArray("libraries"), new TypeToken<List<Library>>() {
            }.getType());
            Map<EntrypointType, Entrypoint> entrypoints = Optional.ofNullable(object.getAsJsonObject("entrypoints")).map(entrypointsRaw ->
                    context.<Map<EntrypointType, Entrypoint>>deserialize(entrypointsRaw, new TypeToken<Map<EntrypointType, Entrypoint>>() {
                    }.getType())
            ).orElseGet(() -> {
                Map<EntrypointType, Entrypoint> builder = new HashMap<>();

                Optional.ofNullable(object.getAsJsonPrimitive("entryPoint")).map(JsonPrimitive::getAsString).ifPresent(entrypoint -> {
                    builder.put(EntrypointType.DBusSession, new Entrypoint(entrypoint, "launchSession"));
                    builder.put(EntrypointType.DBusP2P, new Entrypoint(entrypoint, "launchP2P"));
                });

                Optional.ofNullable(object.getAsJsonPrimitive("bridgedEntryPoint")).map(JsonPrimitive::getAsString).ifPresent(entrypoint -> {
                    builder.put(EntrypointType.Bridge, new Entrypoint(entrypoint, "launch"));
                });

                return builder;
            });

            return new LocalLauncherMeta(version, shortBrand, brand, libraries, entrypoints);
        }
    }
}
