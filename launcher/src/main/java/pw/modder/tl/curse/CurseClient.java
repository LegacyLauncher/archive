package pw.modder.tl.curse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import pw.modder.serialization.InstantSerializer;
import pw.modder.serialization.EnumSerializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class CurseClient {
    private static String BASE_URL = "https://addons-ecs.forgesvc.net/api/v2";
    private static String GAME_ID = "432";
    private static Gson GSON = new GsonBuilder()
            .registerTypeAdapter(AddonFile.DependencyType.class, new EnumSerializer<>())
            .registerTypeAdapter(AddonFile.Status.class, new EnumSerializer<>())
            .registerTypeAdapter(AddonInfo.Status.class, new EnumSerializer<>())
            .registerTypeAdapter(AddonFileReleaseType.class, new EnumSerializer<>())
            .registerTypeAdapter(Instant.class, new InstantSerializer())
            .create();

    public static AddonInfo getAddon(int id) throws IOException {
        return GSON.fromJson(
                Request.Get(BASE_URL + "/addon/" + id)
                        .execute()
                        .returnContent().asString(StandardCharsets.UTF_8),
                AddonInfo.class
        );
    }

    public static List<AddonInfo> getAddons(int ...ids) throws IOException {
        return GSON.fromJson(
                Request.Get(BASE_URL + "/addon")
                    .bodyString(Arrays.toString(ids), ContentType.APPLICATION_JSON)
                    .execute()
                    .returnContent().asString(StandardCharsets.UTF_8),
                List.class
        );
    }

    public static AddonFile getAddonFile(int addonId, int fileId) throws IOException {
        return GSON.fromJson(
                Request.Get(BASE_URL + "/addon/" + addonId + "/files/" + fileId)
                        .execute()
                        .returnContent().asString(StandardCharsets.UTF_8),
                AddonFile.class
        );
    }

    public static List<AddonFile> getAddonFiles(int addonId) throws IOException {
        return GSON.fromJson(
                Request.Get(BASE_URL + "/addon/" + addonId + "/files")
                        .execute()
                        .returnContent().asString(StandardCharsets.UTF_8),
                List.class
        );
    }

    public static String getAddonFileDownloadURL(int addonId, int fileId) throws IOException {
        return Request.Get(BASE_URL + "/addon/" + addonId + "/files/" + fileId + "/download-url")
                .execute()
                .returnContent().asString(StandardCharsets.UTF_8);
    }
}
