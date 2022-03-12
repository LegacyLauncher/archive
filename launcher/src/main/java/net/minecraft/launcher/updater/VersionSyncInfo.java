package net.minecraft.launcher.updater;

import com.google.gson.JsonSyntaxException;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Library;
import net.minecraft.launcher.versions.Rule;
import net.minecraft.launcher.versions.Version;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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

    Set<Downloadable> getRequiredDownloadables(OS os, Rule.FeatureMatcher featureMatcher, File targetDirectory, boolean force, Account.AccountType type, boolean firstIteration) throws IOException {
        Set<Downloadable> neededFiles = new HashSet<>();

        CompleteVersion version0 = getCompleteVersion(force), version;
        if (type != null && !version0.isProceededFor(type.toString())) {
            if (firstIteration) {
                version = TLauncher.getInstance().getLibraryManager().processExplicitly(version0, type.toString());
            } else {
                version = TLauncher.getInstance().getLibraryManager().process(version0, type.toString());
            }
        } else {
            version = version0;
        }

        Repository source = hasRemote() ? remoteVersion.getSource() : null;
        if (source != null && !source.isRemote()) {
            return neededFiles;
        } else {
            Collection<Library> libraries = version.getRelevantLibraries(featureMatcher);

            if (firstIteration) {
                neededFiles.addAll(getRequiredDownloadables(os, featureMatcher, targetDirectory, force, type, false));
            }
            if (type != Account.AccountType.PLAIN) {
                neededFiles.addAll(getRequiredDownloadables(os, featureMatcher, targetDirectory, force, Account.AccountType.PLAIN, false));
            }

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

    public Set<Downloadable> getRequiredDownloadables(Rule.FeatureMatcher featureMatcher, File targetDirectory, boolean force, Account.AccountType type) throws IOException {
        return getRequiredDownloadables(OS.CURRENT, featureMatcher, targetDirectory, force, type, true);
    }

    public static VersionSyncInfo createEmpty() {
        return new VersionSyncInfo();
    }
}
