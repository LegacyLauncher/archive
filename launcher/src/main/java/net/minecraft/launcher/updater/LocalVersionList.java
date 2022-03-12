package net.minecraft.launcher.updater;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;
import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.CurrentLaunchFeatureMatcher;
import net.minecraft.launcher.versions.Rule;
import net.minecraft.launcher.versions.Version;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.pasta.Pasta;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.OS;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Set;

public class LocalVersionList extends StreamVersionList {
    private static final Logger LOGGER = LogManager.getLogger(LocalVersionList.class);

    private final JsonParser jsonParser = new JsonParser();

    private File baseDirectory;
    private File baseVersionsDir;

    public LocalVersionList() throws IOException {
        setBaseDirectory(MinecraftUtil.getWorkingDirectory());
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public File getVersionsDirectory() {
        return baseVersionsDir;
    }

    public void setBaseDirectory(File directory) throws IOException {
        if (directory == null) {
            throw new IllegalArgumentException("Base directory is NULL!");
        } else if (!directory.isDirectory()) {
            throw new IOException("Directory is not yet created!");
        } else if (!directory.canWrite()) {
            throw new IOException("Directory is not accessible!");
        } else {
            baseDirectory = directory;
            baseVersionsDir = new File(baseDirectory, "versions");
        }
    }

    public synchronized void refreshVersions() throws IOException {
        clearCache();
        if (!baseVersionsDir.isDirectory()) {
            FileUtil.createFolder(baseVersionsDir);
        }
        File[] files = baseVersionsDir.listFiles();
        if (files != null) {
            for (File directory : files) {
                String id = directory.getName();
                File jsonFile = new File(directory, id + ".json");
                if (directory.isDirectory() && jsonFile.isFile()) {
                    String input = null;
                    try {
                        try (InputStreamReader reader = getUrl("versions/" + id + "/" + id + ".json")) {
                            input = IOUtils.toString(reader);
                        }
                        if (input.isEmpty()) {
                            LOGGER.warn("Json of {} is empty and is going to be deleted", id);
                            deleteJsonFile(id, jsonFile);
                            continue;
                        }
                        if (StringUtils.containsOnly(input, '\0')) {
                            LOGGER.warn("Json of {} is corrupted and contain only zero bytes. Will try to delete it", id);
                            deleteJsonFile(id, jsonFile);
                            continue;
                        }
                        JsonElement jsonElement = JsonParser.parseString(input);
                        if (!jsonElement.isJsonObject()) {
                            LOGGER.warn("Version doesn't contain object: {}", id);
                            continue;
                        }
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        if (jsonObject.has("modpack") && !jsonObject.get("modpack").isJsonPrimitive()) {
                            LOGGER.debug("Ignoring modpack version: {}", id);
                            continue;
                        }
                        if (!jsonObject.has("id")) {
                            LOGGER.warn("Ignored version without id: {} (probably not a " +
                                    "Minecraft version at all)", id);
                            continue;
                        }
                        CompleteVersion ex = gson.fromJson(jsonObject, CompleteVersion.class);
                        if (ex == null) {
                            LOGGER.warn("Version is empty: {}", id);
                            continue;
                        }
                        ex.setID(id);
                        ex.setSource(Repository.LOCAL_VERSION_REPO);
                        ex.setVersionList(this);
                        addVersion(ex);
                    } catch (Exception e) {
                        if (e.getCause() instanceof MalformedJsonException
                                || e.getCause() instanceof EOFException) {
                            LOGGER.warn("Invalid json file {}", id, e);
                        } else {
                            LOGGER.warn("Could not parse local version \"{}\"", id, e);
                            Sentry.capture(new EventBuilder()
                                    .withMessage("couldn't parse local version")
                                    .withSentryInterface(new ExceptionInterface(e))
                                    .withExtra("version", id)
                                    .withExtra("input", Pasta.pasteJson(input))
                                    .withLevel(Event.Level.ERROR)
                            );
                        }
                        if (e instanceof JsonSyntaxException) {
                            renameJsonFile(jsonFile);
                        }
                    }
                }
            }

        }
    }

    private void deleteJsonFile(String id, File jsonFile) {
        if (jsonFile.delete()) {
            LOGGER.warn("Json of {} deleted successfully", id);
        } else {
            LOGGER.error("Couldn't remove json of {}: {}", id, jsonFile.getAbsolutePath());
        }
    }

    private void renameJsonFile(File jsonFile) {
        String newName = jsonFile.getName() + ".invalid";
        LOGGER.info("Renaming json file: {} -> {}", jsonFile.getAbsolutePath(), newName);

        Path jsonFilePath = jsonFile.toPath();
        try {
            Files.move(jsonFilePath, jsonFilePath.resolveSibling(newName),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("Couldn't rename {}", jsonFile.getAbsolutePath(), e);
        }
    }

    public void saveVersion(CompleteVersion version) throws IOException {
        String text = serializeVersion(version);
        File target = new File(baseVersionsDir, version.getID() + "/" + version.getID() + ".json");
        FileUtil.writeFile(target, text);
    }

    public synchronized void deleteVersion(String id, boolean deleteLibraries) throws IOException {
        Rule.FeatureMatcher featureMatcher = new CurrentLaunchFeatureMatcher();
        CompleteVersion version = getCompleteVersion(id);
        if (version == null) {
            throw new IllegalArgumentException("Version is not installed!");
        } else {
            File dir = new File(baseVersionsDir, id + '/');
            if (!dir.isDirectory()) {
                throw new IOException("Cannot find directory: " + dir.getAbsolutePath());
            } else {
                FileUtil.deleteDirectory(dir);
                if (deleteLibraries) {

                    for (File nativeLib : version.getClassPath(featureMatcher, baseDirectory)) {
                        FileUtil.deleteFile(nativeLib);
                    }

                    for (String nativeLib1 : version.getNatives(featureMatcher)) {
                        FileUtil.deleteFile(new File(baseDirectory, nativeLib1));
                    }

                }
            }
        }
    }

    protected InputStream getInputStream(String uri) throws IOException {
        return new FileInputStream(new File(baseDirectory, uri));
    }

    public boolean hasAllFiles(CompleteVersion version, OS os) {
        Set<String> files = version.getRequiredFiles(os, new CurrentLaunchFeatureMatcher());

        for (String filename : files) {
            File required = new File(baseDirectory, filename);
            if (!required.isFile() || required.length() == 0L) return false;
        }

        return true;
    }

    public synchronized CompleteVersion getCompleteVersion(Version version) throws JsonSyntaxException, IOException {
        if (version instanceof CompleteVersion) {
            return (CompleteVersion) version;
        } else if (version == null) {
            throw new NullPointerException("Version cannot be NULL!");
        } else {
            CompleteVersion complete;
            try (InputStreamReader reader = getUrl("versions/" + version.getID() + "/" + version.getID() + ".json")) {
                complete = gson.fromJson(reader, CompleteVersion.class);
            }
            complete.setID(version.getID());
            complete.setVersionList(this);
            Collections.replaceAll(versions, version, complete);
            return complete;
        }
    }
}
