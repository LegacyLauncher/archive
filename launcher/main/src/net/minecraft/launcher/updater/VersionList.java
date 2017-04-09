package net.minecraft.launcher.updater;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.PartialVersion;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;
import ru.turikhay.util.json.ExposeExclusion;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

public abstract class VersionList {
    protected final Gson gson;
    protected final Map<String, Version> byName = new Hashtable();
    protected final List<Version> versions = Collections.synchronizedList(new ArrayList());
    protected final Map<ReleaseType, Version> latest = new Hashtable();

    private final List<VersionList> dependencies = new ArrayList<VersionList>();

    VersionList() {
        GsonBuilder builder = new GsonBuilder();
        ExposeExclusion.setup(builder);
        builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
        builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
        builder.registerTypeAdapter(CompleteVersion.class, new CompleteVersion.CompleteVersionSerializer());
        builder.enableComplexMapKeySerialization();
        builder.setPrettyPrinting();
        gson = builder.create();
    }

    public List<Version> getVersions() {
        return versions;
    }

    public Map<ReleaseType, Version> getLatestVersions() {
        return latest;
    }

    public Version getVersion(String name) {
        if (name != null && !name.isEmpty()) {
            return byName.get(name);
        } else {
            throw new IllegalArgumentException("Name cannot be NULL or empty");
        }
    }

    public CompleteVersion getCompleteVersion(Version version) throws JsonSyntaxException, IOException {
        if (version instanceof CompleteVersion) {
            return (CompleteVersion) version;
        } else if (version == null) {
            throw new NullPointerException("Version cannot be NULL!");
        } else {
            CompleteVersion complete = gson.fromJson(version.getUrl() == null ? getUrl("versions/" + version.getID() + "/" + version.getID() + ".json") : new InputStreamReader(new URL(version.getUrl()).openConnection(U.getProxy()).getInputStream(), FileUtil.DEFAULT_CHARSET), CompleteVersion.class);
            complete.setID(version.getID());
            complete.setVersionList(this);
            Collections.replaceAll(versions, version, complete);
            return complete;
        }
    }

    public CompleteVersion getCompleteVersion(String name) throws JsonSyntaxException, IOException {
        Version version = getVersion(name);
        return version == null ? null : getCompleteVersion(version);
    }

    public Version getLatestVersion(ReleaseType type) {
        if (type == null) {
            throw new NullPointerException();
        } else {
            return latest.get(type);
        }
    }

    public RawVersionList getRawList() throws IOException {
        Object lock = new Object();
        Time.start(lock);
        RawVersionList list = gson.fromJson(getUrl("versions/versions.json"), RawVersionList.class);
        Iterator var4 = list.versions.iterator();

        while (var4.hasNext()) {
            PartialVersion version = (PartialVersion) var4.next();
            version.setVersionList(this);
        }

        log("Got in", Time.stop(lock), "ms");
        return list;
    }

    public void refreshVersions(RawVersionList versionList) {
        clearCache();
        Iterator var3 = versionList.getVersions().iterator();

        while (var3.hasNext()) {
            Version en = (Version) var3.next();
            if (en != null && en.getID() != null) {
                versions.add(en);
                byName.put(en.getID(), en);
            }
        }

        var3 = versionList.latest.entrySet().iterator();

        while (var3.hasNext()) {
            Entry en1 = (Entry) var3.next();
            ReleaseType releaseType = (ReleaseType) en1.getKey();
            if (releaseType == null) {
                log("Unknown release type for latest version entry:", en1);
            } else {
                Version version = getVersion((String) en1.getValue());

                if (version == null) {
                    log("Cannot find version for latest version entry: " + en1);
                } else {
                    latest.put(releaseType, version);
                }
            }
        }

    }

    public void refreshVersions() throws IOException {
        refreshVersions(getRawList());
    }

    CompleteVersion addVersion(CompleteVersion version) {
        if (version.getID() == null) {
            throw new IllegalArgumentException("Cannot add blank version");
        } else if (getVersion(version.getID()) != null) {
            log("Version \'" + version.getID() + "\' is already tracked");
            return version;
        } else {
            versions.add(version);
            byName.put(version.getID(), version);
            return version;
        }
    }

    void removeVersion(Version version) {
        if (version == null) {
            throw new NullPointerException("Version cannot be NULL!");
        } else {
            versions.remove(version);
            byName.remove(version);
        }
    }

    public void removeVersion(String name) {
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

    void clearCache() {
        byName.clear();
        versions.clear();
        latest.clear();
    }

    public final List<VersionList> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    public final void addDependancy(VersionList list) {
        if (U.requireNotNull(list) == this) {
            throw new IllegalArgumentException("cannot be itself");
        }

        if (list.getDependencies().contains(this)) {
            throw new IllegalArgumentException("invalid nesting");
        }

        dependencies.add(list);
    }

    void log(Object... obj) {
        U.log("[" + getClass().getSimpleName() + "]", obj);
    }
}
