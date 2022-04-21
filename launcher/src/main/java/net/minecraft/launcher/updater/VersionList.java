package net.minecraft.launcher.updater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.PartialVersion;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.repository.RepositoryProxy;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.Time;
import ru.turikhay.util.json.ExposeExclusion;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

public abstract class VersionList {
    private static final Logger LOGGER = LogManager.getLogger();

    protected final Gson gson;
    protected final Map<String, Version> byName = new HashMap<>();
    protected final List<Version> versions = Collections.synchronizedList(new ArrayList<>());
    protected final Map<ReleaseType, Version> latest = new HashMap<>();

    private final List<VersionList> dependencies = new ArrayList<>();

    VersionList() {
        GsonBuilder builder = new GsonBuilder();
        ExposeExclusion.setup(builder);
        builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
        builder.registerTypeAdapter(Date.class, new DateTypeAdapter(true));
        builder.registerTypeAdapter(CompleteVersion.class, new CompleteVersion.CompleteVersionSerializer());
        builder.enableComplexMapKeySerialization();
        builder.setPrettyPrinting();
        gson = builder.create();
    }

    public synchronized List<Version> getVersions() {
        return new ArrayList<>(versions);
    }

    public synchronized Map<ReleaseType, Version> getLatestVersions() {
        return new HashMap<>(latest);
    }

    public synchronized Version getVersion(String name) {
        if (name != null && !name.isEmpty()) {
            return byName.get(name);
        } else {
            throw new IllegalArgumentException("Name cannot be NULL or empty");
        }
    }

    public synchronized CompleteVersion getCompleteVersion(Version version) throws JsonSyntaxException, IOException {
        if (version instanceof CompleteVersion) {
            return (CompleteVersion) version;
        } else if (version == null) {
            throw new NullPointerException("Version cannot be NULL!");
        } else {
            CompleteVersion complete;
            InputStreamReader reader = null;
            try {
                if (version.getUrl() == null) {
                    reader = getUrl("versions/" + version.getID() + "/" + version.getID() + ".json");
                } else {
                    URL url = new URL(version.getUrl());
                    InputStream input;
                    if (RepositoryProxy.canBeProxied(url)) {
                        input = RepositoryProxy.getProxyRepoList().read(url.toString());
                    } else {
                        input = url.openConnection().getInputStream();
                    }
                    reader = new InputStreamReader(input, FileUtil.DEFAULT_CHARSET);
                }
                complete = gson.fromJson(reader, CompleteVersion.class);
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
            complete.setID(version.getID());
            complete.setVersionList(this);
            Collections.replaceAll(versions, version, complete);
            return complete;
        }
    }

    public synchronized CompleteVersion getCompleteVersion(String name) throws JsonSyntaxException, IOException {
        Version version = getVersion(name);
        return version == null ? null : getCompleteVersion(version);
    }

    public synchronized Version getLatestVersion(ReleaseType type) {
        if (type == null) {
            throw new NullPointerException();
        } else {
            return latest.get(type);
        }
    }

    public RawVersionList getRawList() throws IOException {
        Object lock = new Object();
        Time.start(lock);
        RawVersionList list;
        String input;
        try (InputStreamReader reader = getUrl("versions/versions.json")) {
            input = IOUtils.toString(reader);
        }
        try {
            list = gson.fromJson(input, RawVersionList.class);
        } catch (RuntimeException e) {
            Sentry.capture(new EventBuilder()
                    .withLevel(Event.Level.ERROR)
                    .withMessage("couldn't parse remote repository")
                    .withSentryInterface(new ExceptionInterface(e))
            );
            throw e;
        }

        for (PartialVersion version : list.versions) {
            version.setVersionList(this);
        }

        LOGGER.info("Got {} in {} ms", getClass().getSimpleName(), Time.stop(lock));
        return list;
    }

    public synchronized void refreshVersions(RawVersionList versionList) {
        clearCache();

        for (PartialVersion partialVersion : versionList.getVersions()) {
            if (partialVersion != null && partialVersion.getID() != null) {
                versions.add(partialVersion);
                byName.put(partialVersion.getID(), partialVersion);
            }
        }

        for (Entry<ReleaseType, String> entry : versionList.latest.entrySet()) {
            ReleaseType releaseType = entry.getKey();
            if (releaseType == null) {
                LOGGER.warn("Unknown release type for latest version entry: {}", entry);
            } else {
                Version version = getVersion(entry.getValue());

                if (version == null) {
                    LOGGER.warn("Cannot find version for latest version entry: {}", entry);
                } else {
                    latest.put(releaseType, version);
                }
            }
        }

    }

    public synchronized void refreshVersions() throws IOException {
        refreshVersions(getRawList());
    }

    synchronized CompleteVersion addVersion(CompleteVersion version) {
        if (version.getID() == null) {
            throw new IllegalArgumentException("Cannot add blank version");
        } else if (getVersion(version.getID()) != null) {
            LOGGER.warn("Version '{}' is already tracked", version.getID());
            return version;
        } else {
            versions.add(version);
            byName.put(version.getID(), version);
            return version;
        }
    }

    synchronized void removeVersion(Version version) {
        if (version == null) {
            throw new NullPointerException("Version cannot be NULL!");
        } else {
            versions.remove(version);
            byName.remove(version.getID());
        }
    }

    public synchronized void removeVersion(String name) {
        Version version = getVersion(name);
        if (version != null) {
            removeVersion(version);
        }
    }

    public String serializeVersion(CompleteVersion version) {
        if (version == null) {
            throw new NullPointerException("CompleteVersion cannot be NULL!");
        } else {
            return gson.toJson(version);
        }
    }

    public abstract boolean hasAllFiles(CompleteVersion var1, OS var2);

    protected abstract InputStreamReader getUrl(String var1) throws IOException;

    synchronized void clearCache() {
        byName.clear();
        versions.clear();
        latest.clear();
    }

    public final List<VersionList> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    public final synchronized void addDependancy(VersionList list) {
        if (Objects.requireNonNull(list) == this) {
            throw new IllegalArgumentException("cannot be itself");
        }

        if (list.getDependencies().contains(this)) {
            throw new IllegalArgumentException("invalid nesting");
        }

        dependencies.add(list);
    }
}
