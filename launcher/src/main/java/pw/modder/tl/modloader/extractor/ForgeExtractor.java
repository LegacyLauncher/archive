package pw.modder.tl.modloader.extractor;

import com.google.gson.annotations.Expose;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Library;
import org.apache.commons.io.IOUtils;
import ru.turikhay.util.U;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ForgeExtractor {
    // Json inside installer jar
    // Must run processors to patch Minecraft
    private ForgeExtractor() {
    }

    public static pw.modder.tl.modloader.extractor.ForgeProcessor ExtractProfile(File file, File librariesDir, File versionsDir) throws IOException {
        ZipFile zipFile = new ZipFile(file);

        ZipEntry profileEntry = zipFile.getEntry("install_profile.json");
        if (profileEntry == null) throw new IOException(zipFile.getName() + ": cannot load install_profile.json");

        InstallProfile profile = U.getGson().fromJson(
                IOUtils.toString(zipFile.getInputStream(profileEntry), StandardCharsets.UTF_8),
                InstallProfile.class
        );

        if (profile.getVersionInfo() != null) {
            return new pw.modder.tl.modloader.extractor.ForgeProcessor(profile, librariesDir, versionsDir);
        }

        if (profile.getJson() != null && zipFile.getEntry(profile.getJson()) != null) {
            ZipEntry versionEntry = zipFile.getEntry(profile.getJson());
            if (versionEntry != null) {
                profile.setJsonData(
                        U.getGson().fromJson(
                                IOUtils.toString(zipFile.getInputStream(profileEntry), StandardCharsets.UTF_8),
                                CompleteVersion.class
                        )
                );
                return new pw.modder.tl.modloader.extractor.ForgeProcessor(profile, librariesDir, versionsDir);
            }
        }

        throw new IOException("Cannot parse " + file.getName() + ", unknown format");
    }

    public static class InstallProfile {
        // old manifest only
        private Install install;
        private VersionInfo versionInfo;

        // new manifest only
        private String path, json, minecraft;
        private Map<String, ForgeData> data;
        private List<ForgeProcessor> processors;
        private List<ProcessorLibrary> libraries;
        @Expose(serialize = false, deserialize = false)
        private CompleteVersion jsonData;

        public Install getInstall() {
            return install;
        }

        public String getMinecraft() {
            if (install == null) return minecraft;
            return install.minecraft;
        }

        public VersionInfo getVersionInfo() {
            return versionInfo;
        }

        public String getPath() {
            return path;
        }

        public String getJson() {
            return json;
        }

        void setJsonData(CompleteVersion jsonData) {
            this.jsonData = jsonData;
        }

        public CompleteVersion getJsonData() {
            return jsonData;
        }

        public List<ForgeProcessor> getProcessors() {
            return processors;
        }

        public List<ProcessorLibrary> getLibraries() {
            return libraries;
        }

        public Map<String, ForgeData> getData() {
            return data;
        }
    }

    public static class ProcessorLibrary {
        private String name;
        private Downloads downloads;

        public String getName() {
            return name;
        }

        public String getPath() {
            return downloads.artifact.path;
        }

        public String getUrl() {
            return downloads.artifact.url;
        }

        public long getSize() {
            return downloads.artifact.size;
        }

        public String getSha1() {
            return downloads.artifact.sha1;
        }
    }

    private static class Downloads {
        private Artifact artifact;
    }

    private static class Artifact {
        private String path, url, sha1;
        private long size;
    }

    static class Install {
        private String profileName;
        private String version;
        private String minecraft;

        public String getProfileName() {
            return profileName;
        }

        public String getVersion() {
            return version;
        }

        public String getMinecraft() {
            return minecraft;
        }
    }

    public static class VersionInfo {
        private String id, minecraftArguments, mainClass, assets, jar;
        private Date time, releaseDate;
        private Integer minimumLauncherVersion;
        private List<VersionInfoLibrary> libraries;

        public CompleteVersion getCompleteVersion() {
            return new CompleteVersion(
                    id, minecraftArguments, mainClass, assets, jar, time, releaseDate, minimumLauncherVersion,
                    libraries.stream()
                            .filter(VersionInfoLibrary::isClient)
                            .map(VersionInfoLibrary::getLibrary)
                            .collect(Collectors.toList())
            );
        }
    }

    private static class VersionInfoLibrary {
        private String name, url;
        private Boolean clientreq, serverreq;
        private List<String> checksums;

        public Library getLibrary() {
            return new Library(name, url, checksums.stream().findFirst().orElse(null));
        }

        public boolean isClient() {
            return clientreq != null && clientreq;
        }
    }

    public static class ForgeData {
        private String client, server;

        public String getClient() {
            return client;
        }

        public String getServer() {
            return server;
        }
    }

    public static class ForgeProcessor {
        private String jar;
        private List<String> classpath, args;
        private Map<String, String> output; // can be null

        public String getJar() {
            return jar;
        }

        public List<String> getClasspath() {
            return classpath;
        }

        public List<String> getArgs() {
            return args;
        }

        public Map<String, String> getOutput() {
            return output;
        }
    }
}
