package net.minecraft.launcher.updater;

import com.google.gson.JsonSyntaxException;
import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.downloader.Downloadable;
import net.legacylauncher.managers.VersionManager;
import net.legacylauncher.minecraft.auth.Account;
import net.legacylauncher.repository.Repository;
import net.legacylauncher.util.FileUtil;
import net.legacylauncher.util.OS;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Library;
import net.minecraft.launcher.versions.Rule;
import net.minecraft.launcher.versions.Version;
import org.checkerframework.checker.units.qual.A;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class VersionSyncInfo {
    protected Version localVersion;
    protected Version remoteVersion;
    private CompleteVersion completeLocal;
    private CompleteVersion completeRemote;
    private String id;

    public VersionSyncInfo(Version localVersion, Version remoteVersion) {
        if (localVersion == null && remoteVersion == null) {
            throw new NullPointerException("Cannot create sync info from NULLs!");
        } else {
            this.localVersion = localVersion;
            this.remoteVersion = remoteVersion;
            if (localVersion != null && remoteVersion != null) {
                localVersion.setVersionList(remoteVersion.getVersionList());
            }

            if (getID() == null) {
                throw new NullPointerException("Cannot create sync info from versions that have NULL IDs");
            }
        }
    }

    public VersionSyncInfo(VersionSyncInfo info) {
        this(info.getLocal(), info.getRemote());
    }

    protected VersionSyncInfo() {
        localVersion = null;
        remoteVersion = null;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (getID() != null && o instanceof VersionSyncInfo) {
            VersionSyncInfo v = (VersionSyncInfo) o;
            return getID().equals(v.getID());
        } else {
            return false;
        }
    }

    public Version getLocal() {
        return localVersion;
    }

    public void setLocal(Version version) {
        localVersion = version;
        if (version instanceof CompleteVersion) {
            completeLocal = (CompleteVersion) version;
        }

    }

    public Version getRemote() {
        return remoteVersion;
    }

    public void setRemote(Version version) {
        remoteVersion = version;
        if (version instanceof CompleteVersion) {
            completeRemote = (CompleteVersion) version;
        }

    }

    public String getID() {
        return id != null ? id : (localVersion != null ? localVersion.getID() : (remoteVersion != null ? remoteVersion.getID() : null));
    }

    public void setID(String id) {
        if (id != null && id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be empty!");
        } else {
            this.id = id;
        }
    }

    public Version getLatestVersion() {
        return remoteVersion != null ? remoteVersion : localVersion;
    }

    public Version getAvailableVersion() {
        return localVersion != null ? localVersion : remoteVersion;
    }

    public boolean isInstalled() {
        return localVersion != null;
    }

    public boolean hasRemote() {
        return remoteVersion != null;
    }

    public boolean isUpToDate() {
        return localVersion != null && (remoteVersion == null || localVersion.getUpdatedTime().compareTo(remoteVersion.getUpdatedTime()) >= 0);
    }

    public String toString() {
        return getClass().getSimpleName() + "{id='" + getID() + "',\nlocal=" + localVersion + ",\nremote=" + remoteVersion + ", isInstalled=" + isInstalled() + ", hasRemote=" + hasRemote() + ", isUpToDate=" + isUpToDate() + "}";
    }

    public CompleteVersion resolveCompleteVersion(VersionManager manager, boolean latest) throws IOException {
        Version version;
        if (latest) {
            version = getLatestVersion();
        } else if (isInstalled()) {
            version = getLocal();
        } else {
            version = getRemote();
        }

        if (version.equals(localVersion) && completeLocal != null && completeLocal.getInheritsFrom() == null) {
            return completeLocal;
        } else if (version.equals(remoteVersion) && completeRemote != null && completeRemote.getInheritsFrom() == null) {
            return completeRemote;
        } else {
            CompleteVersion complete;
            try {
                complete = version.getVersionList()
                        .getCompleteVersion(version)
                        .resolve(manager, latest);
            } catch (JsonSyntaxException e) {
                throw new IOException("syntax error resolving " + version.getID(), e);
            }
            if (version == localVersion) {
                completeLocal = complete;
            } else if (version == remoteVersion) {
                completeRemote = complete;
            }

            return complete;
        }
    }

    public CompleteVersion getCompleteVersion(boolean latest) throws IOException {
        Version version;
        if (latest) {
            version = getLatestVersion();
        } else if (isInstalled()) {
            version = getLocal();
        } else {
            version = getRemote();
        }

        if (version.equals(localVersion) && completeLocal != null) {
            return completeLocal;
        } else if (version.equals(remoteVersion) && completeRemote != null) {
            return completeRemote;
        } else {
            CompleteVersion complete = version.getVersionList().getCompleteVersion(version);
            if (version == localVersion) {
                completeLocal = complete;
            } else if (version == remoteVersion) {
                completeRemote = complete;
            }
            return complete;
        }
    }

    public CompleteVersion getLatestCompleteVersion() throws IOException {
        return getCompleteVersion(true);
    }

    public CompleteVersion getLocalCompleteVersion() {
        if (completeLocal == null && localVersion instanceof CompleteVersion) {
            completeLocal = (CompleteVersion) localVersion;
        }
        return completeLocal;
    }

    Set<Downloadable> getRequiredDownloadables(OS os, Rule.FeatureMatcher featureMatcher, File targetDirectory, boolean force, String[] types) throws IOException {
        if (types == null) {
            types = new String[] { Account.AccountType.PLAIN.toString() };
        }
        if (Arrays.stream(types).noneMatch(it -> Account.AccountType.PLAIN.toString().equals(it))) {
            String[] newTypes = Arrays.copyOf(types, types.length + 1);
            newTypes[newTypes.length - 1] = Account.AccountType.PLAIN.toString();
            types = newTypes;
        }

        Set<Downloadable> neededFiles = new HashSet<>();

        CompleteVersion version0 = getCompleteVersion(force), version;
        ArrayList<String> unprocessedTypes = new ArrayList<>();
        Arrays.stream(types).filter(it -> !version0.isProceededFor(it)).collect(Collectors.toCollection(() -> unprocessedTypes));

        if (unprocessedTypes.isEmpty()) {
            version = version0;
        } else {
            version = LegacyLauncher.getInstance().getLibraryManager().process(version0, unprocessedTypes.toArray(new String[0]));
        }

        Repository source = hasRemote() ? remoteVersion.getSource() : null;
        if (source != null && !source.isRemote()) {
            return neededFiles;
        } else {
            Collection<Library> libraries = version.getRelevantLibraries(featureMatcher);

            Iterator<Library> var9 = libraries.iterator();
            while (true) {
                Library library;
                File local1;
                do {
                    String file;
                    do {
                        if (!var9.hasNext()) {
                            return neededFiles;
                        }

                        library = var9.next();
                        file = null;
                        if (library.getNatives() != null) {
                            String local = library.getNatives().get(os);
                            if (local != null) {
                                file = library.getArtifactPath(local);
                            }
                        } else {
                            file = library.getArtifactPath();
                        }
                    } while (file == null);

                    local1 = new File(targetDirectory, "libraries/" + file);
                }
                while (!force && local1.isFile() && (library.getChecksum() == null || library.getChecksum().equals(FileUtil.getChecksum(local1, "SHA-1"))));

                if (!library.hasEmptyUrl()) {
                    neededFiles.add(library.getDownloadable(source, featureMatcher, local1, os));
                }
            }
        }
    }

    public Set<Downloadable> getRequiredDownloadables(Rule.FeatureMatcher featureMatcher, File targetDirectory, boolean force, String[] types) throws IOException {
        return getRequiredDownloadables(OS.CURRENT, featureMatcher, targetDirectory, force, types);
    }

    public static VersionSyncInfo createEmpty() {
        return new VersionSyncInfo();
    }
}
