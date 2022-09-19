package pw.modder.tl.modloader;

import net.minecraft.launcher.versions.CompleteVersion;
import org.apache.http.client.fluent.Request;
import pw.modder.http.HttpClientUtils;
import pw.modder.tl.modloader.extractor.ForgeExtractor;
import pw.modder.tl.modloader.extractor.ForgeProcessor;
import ru.turikhay.util.U;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public interface Modloader {
    CompleteVersion install(String versionName, String versionFamily, File versionsDir, File librariesDir) throws IOException;

    ModloaderFamily getFamily();

    String getMinecraftVersion();

    String getModloaderVersion();

    class Forge implements Modloader {
        private static final String FORGE_INSTALLER_URL = "https://files.minecraftforge.net/maven/net/minecraftforge/forge/%1$s-%2$s/forge-%1$s-%2$s-installer.jar";
        // Promotions (latest/recommended)
        // https://files.minecraftforge.net/maven/net/minecraftforge/forge/promotions.json
        // All versions list
        // https://files.minecraftforge.net/maven/net/minecraftforge/forge/maven-metadata.json
        // https://files.minecraftforge.net/maven/net/minecraftforge/forge/maven-metadata.xml

        private final String minecraft, version, url;

        public Forge(String minecraft, String version) {
            this.minecraft = minecraft;
            this.version = version;
            this.url = String.format(Locale.ROOT, FORGE_INSTALLER_URL, minecraft, version);
        }

        public Forge(String minecraft, String version, String url) {
            this.minecraft = minecraft;
            this.version = version;
            this.url = url;
        }

        @Override
        public CompleteVersion install(String versionName, String versionFamily, File versionsDir, File librariesDir) throws IOException {
            File tempFile = File.createTempFile("forge", ".jar");
            HttpClientUtils.execute(Request.Get(url))
                    .saveContent(tempFile);

            ForgeProcessor fpr = ForgeExtractor.ExtractProfile(tempFile, librariesDir, versionsDir);

            fpr.downloadLibraries();
            try {
                fpr.runProcessors();
            } catch (InterruptedException e) {
                e.printStackTrace();
                // TODO DO SOMETHING
            }

            CompleteVersion ver = fpr.getCompleteVersion();
            ver.setID(versionName);
            ver.setFamily(versionFamily);

            return ver;
        }

        @Override
        public ModloaderFamily getFamily() {
            return ModloaderFamily.FORGE;
        }

        @Override
        public String getMinecraftVersion() {
            return minecraft;
        }

        @Override
        public String getModloaderVersion() {
            return version;
        }
    }

//    class OptiFine implements Modloader {
//
//    }

//    class Liteloader implements Modloader {
//        // version manifest
//        // http://dl.liteloader.com/versions/versions.json
//    }

    class Fabric implements Modloader {
        private final String minecraft, version;
        private static final String API_META_URL = "https://meta.fabricmc.net/v2/versions/loader/%s/%s/profile/json";

        public Fabric(String minecraft, String version) {
            this.minecraft = minecraft;
            this.version = version;
        }

        @Override
        public ModloaderFamily getFamily() {
            return ModloaderFamily.FABRIC;
        }

        @Override
        public String getMinecraftVersion() {
            return minecraft;
        }

        @Override
        public String getModloaderVersion() {
            return version;
        }

        @Override
        public CompleteVersion install(String versionName, String versionFamily, File versionsDir, File librariesDir) throws IOException {
            CompleteVersion completeVersion = U.getGson().fromJson(
                    HttpClientUtils.execute(Request.Get(String.format(Locale.ROOT, API_META_URL, minecraft, version)))
                            .returnContent().asString(),
                    CompleteVersion.class
            );
            completeVersion.setID(versionName);
            completeVersion.setFamily(versionFamily);
            return completeVersion;
        }
    }
}
