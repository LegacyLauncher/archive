package pw.modder.tl.curse;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import pw.modder.http.HttpClientUtils;
import pw.modder.serialization.EnumSerializer;
import pw.modder.serialization.InstantSerializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class CurseClient {
    private static final String BASE_URL = "https://addons-ecs.forgesvc.net/api/v2";
    private static final String GAME_ID = "432";
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(AddonFile.DependencyType.class, new EnumSerializer<>(AddonFile.DependencyType.class))
            .registerTypeAdapter(AddonFile.Status.class, new EnumSerializer<>(AddonFile.Status.class))
            .registerTypeAdapter(AddonInfo.Status.class, new EnumSerializer<>(AddonInfo.Status.class))
            .registerTypeAdapter(AddonFileReleaseType.class, new EnumSerializer<>(AddonFileReleaseType.class))
            .registerTypeAdapter(Instant.class, new InstantSerializer())
            .create();

    public static AddonInfo getAddon(int id) throws IOException {
        return GSON.fromJson(
                HttpClientUtils.execute(Request.Get(BASE_URL + "/addon/" + id))
                        .returnContent().asString(StandardCharsets.UTF_8),
                AddonInfo.class
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    public static List<AddonInfo> getAddons(int... ids) throws IOException {
        return GSON.fromJson(
                HttpClientUtils.execute(
                        Request.Get(BASE_URL + "/addon")
                                .bodyString(Arrays.toString(ids), ContentType.APPLICATION_JSON)
                ).returnContent().asString(StandardCharsets.UTF_8),
                new TypeToken<List<AddonInfo>>() {
                }.getType()
        );
    }

    public static AddonFile getAddonFile(int addonId, int fileId) throws IOException {
        return GSON.fromJson(
                HttpClientUtils.execute(Request.Get(BASE_URL + "/addon/" + addonId + "/files/" + fileId))
                        .returnContent().asString(StandardCharsets.UTF_8),
                AddonFile.class
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    public static List<AddonFile> getAddonFiles(int addonId) throws IOException {
        return GSON.fromJson(
                HttpClientUtils.execute(Request.Get(BASE_URL + "/addon/" + addonId + "/files"))
                        .returnContent().asString(StandardCharsets.UTF_8),
                new TypeToken<List<AddonFile>>() {
                }.getType()
        );
    }

    public static String getAddonFileDownloadURL(int addonId, int fileId) throws IOException {
        return HttpClientUtils.execute(
                Request.Get(BASE_URL + "/addon/" + addonId + "/files/" + fileId + "/download-url")
        ).returnContent().asString(StandardCharsets.UTF_8);
    }
}
