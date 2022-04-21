package pw.modder.tl.modloader;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import net.minecraft.launcher.versions.Library;
import org.apache.http.client.fluent.Request;
import pw.modder.http.HttpClientUtils;
import ru.turikhay.util.U;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public interface ModloaderList {
    List<String> list(String minecraftVersion);

    Optional<String> getStable(String minecraftVersion);

    Optional<String> getLatest(String minecraftVersion);

    void fetch() throws IOException;

    class Forge implements ModloaderList {
        private final static String FORGE_MAVEN_METADATA_URL = "https://files.minecraftforge.net/maven/net/minecraftforge/forge/maven-metadata.json";
        private final static String FORGE_PROMOTIONS_URL = "https://files.minecraftforge.net/maven/net/minecraftforge/forge/promotions_slim.json";
        private final static Type METADATA_TYPE = new TypeToken<Map<String, List<String>>>() {
        }.getType();

        private Map<String, List<String>> versions = Collections.emptyMap();
        private Promotions promotions = new Promotions();

        @Override
        public List<String> list(String minecraftVersion) {
            return versions.getOrDefault(minecraftVersion, Collections.emptyList());
        }

        @Override
        public Optional<String> getStable(String minecraftVersion) {
            return promotions.getRecommended(minecraftVersion);
        }

        @Override
        public Optional<String> getLatest(String minecraftVersion) {
            return promotions.getLatest(minecraftVersion);
        }

        @Override
        public void fetch() throws IOException {
            versions = U.getGson().fromJson(
                    HttpClientUtils.execute(Request.Get(FORGE_MAVEN_METADATA_URL))
                            .returnContent().asString(StandardCharsets.UTF_8),
                    METADATA_TYPE
            );
            promotions = U.getGson().fromJson(
                    HttpClientUtils.execute(Request.Get(FORGE_PROMOTIONS_URL))
                            .returnContent().asString(StandardCharsets.UTF_8),
                    Promotions.class
            );
        }

        private static class Promotions {
            private final Map<String, String> promos = Collections.emptyMap();

            public Optional<String> getRecommended(String minecraft) {
                if (promos.containsKey(minecraft + "-recommended"))
                    return Optional.of(promos.get(minecraft + "-recommended"));
                return Optional.empty();
            }

            public Optional<String> getLatest(String minecraft) {
                if (promos.containsKey(minecraft + "-latest"))
                    return Optional.of(promos.get(minecraft + "-recommended"));
                return Optional.empty();
            }
        }
    }

    // TODO
    class LiteLoader implements ModloaderList {
        private static final String VERSIONS_LIST_URL = "http://dl.liteloader.com/versions/versions.json";
        private Versions versions;

        @Override
        public List<String> list(String minecraftVersion) {
            Optional<LiteLoaderVersion> list = versions.getVersion(minecraftVersion);
            return list.map(liteLoaderVersion -> liteLoaderVersion.getAll().stream()
                            .map(LiteLoaderVersionEntry::getVersion)
                            .collect(Collectors.toList()))
                    .orElse(Collections.emptyList());
        }

        @Override
        public Optional<String> getStable(String minecraftVersion) {
            return versions.getVersion(minecraftVersion)
                    .map(LiteLoaderVersion::getReleases)
                    .map(LiteLoaderVersionMeta::getLatest)
                    .flatMap(it -> it)
                    .map(LiteLoaderVersionEntry::getVersion);
        }

        // can work wrong
        @Override
        public Optional<String> getLatest(String minecraftVersion) {
            return versions.getVersion(minecraftVersion)
                    .map(LiteLoaderVersion::getSnapshots)
                    .map(LiteLoaderVersionMeta::getLatest)
                    .flatMap(it -> it)
                    .map(LiteLoaderVersionEntry::getVersion);
        }

        @Override
        public void fetch() throws IOException {
            U.getGson().fromJson(
                    HttpClientUtils.execute(Request.Get(VERSIONS_LIST_URL))
                            .returnContent().asString(),
                    Versions.class
            );
        }

        static class Versions {
            private Map<String, LiteLoaderVersion> versions;

            public Optional<LiteLoaderVersion> getVersion(String minecraft) {
                if (versions.containsKey(minecraft)) return Optional.of(versions.get(minecraft));
                return Optional.empty();
            }
        }

        private static class LiteLoaderVersion {
            private LiteLoaderVersionMeta artifacts; // not a typo
            private LiteLoaderVersionMeta snapshots;
            private LiteLoaderVersionRepo repo;

            public LiteLoaderVersionMeta getSnapshots() {
                return snapshots;
            }

            public LiteLoaderVersionMeta getReleases() {
                return artifacts;
            }

            public List<LiteLoaderVersionEntry> getAll() {
                List<LiteLoaderVersionEntry> vers = new ArrayList<>(artifacts.versions.size() + snapshots.versions.size());
                vers.addAll(artifacts.versions.values());
                vers.addAll(snapshots.versions.values());
                return vers;
            }
        }

        private static class LiteLoaderVersionRepo {
            private String url;
            private LiteLoaderStream stream;
        }

        private static class LiteLoaderVersionMeta {
            @SerializedName("com.mumfrey:liteloader")
            private Map<String, LiteLoaderVersionEntry> versions;

            public Optional<LiteLoaderVersionEntry> getEntry(String md5) {
                if (versions.containsKey(md5)) return Optional.of(versions.get(md5));
                return Optional.empty();
            }

            public Optional<LiteLoaderVersionEntry> getLatest() {
                return getEntry("latest");
            }
        }

        private static class LiteLoaderVersionEntry {
            private String tweakClass, file, version, md5;
            private List<Library> libraries;
            private LiteLoaderStream stream;

            public boolean isStable() {
                return stream == LiteLoaderStream.RELEASE;
            }

            public String getTweakClass() {
                return tweakClass;
            }

            public String getFile() {
                return file;
            }

            public String getVersion() {
                return version;
            }

            public String getMd5() {
                return md5;
            }

            public List<Library> getLibraries() {
                return libraries;
            }
        }

        enum LiteLoaderStream {
            SNAPSHOT, RELEASE
        }
    }

    // TODO
    class Fabric implements ModloaderList {
        private static final String FABRIC_LIST_URL = "https://meta.fabricmc.net/v2/versions/loader";
        private final static Type METADATA_TYPE = new TypeToken<List<FabricLoaderMeta>>() {
        }.getType();
        private List<FabricLoaderMeta> versions = Collections.emptyList();

        @Override
        public List<String> list(String minecraftVersion) {
            // Fabric installer allows installing any Fabric version for any Minecraft version, so...
            return versions.stream().map(FabricLoaderMeta::getVersion).collect(Collectors.toList());
        }

        @Override
        public Optional<String> getStable(String minecraftVersion) {
            return versions.stream()
                    .filter(FabricLoaderMeta::isStable)
                    .findFirst()
                    .map(FabricLoaderMeta::getVersion);
        }

        @Override
        public Optional<String> getLatest(String minecraftVersion) {
            return versions.stream()
                    .max(Comparator.comparing(FabricLoaderMeta::getBuild))
                    .map(FabricLoaderMeta::getVersion);
        }

        @Override
        public void fetch() throws IOException {
            versions = U.getGson().fromJson(
                    HttpClientUtils.execute(Request.Get(FABRIC_LIST_URL))
                            .returnContent().asString(StandardCharsets.UTF_8),
                    METADATA_TYPE
            );
        }

        private static class FabricLoaderMeta {
            private String separator, maven, version;
            private int build;
            private boolean stable;

            public String getSeparator() {
                return separator;
            }

            public String getMaven() {
                return maven;
            }

            public String getVersion() {
                return version;
            }

            public int getBuild() {
                return build;
            }

            public boolean isStable() {
                return stable;
            }
        }
    }
}
