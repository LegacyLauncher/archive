package net.minecraft.launcher.updater;

import com.google.gson.JsonSyntaxException;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Version;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.OS;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

public class RepositoryBasedVersionList extends RemoteVersionList {
    private final Repository repository;

    RepositoryBasedVersionList(Repository repository) {
        if (repository == null) {
            throw new NullPointerException();
        } else {
            this.repository = repository;
        }
    }

    public VersionList.RawVersionList getRawList() throws IOException {
        VersionList.RawVersionList rawList = super.getRawList();
        Iterator var3 = rawList.getVersions().iterator();

        while (var3.hasNext()) {
            Version version = (Version) var3.next();
            version.setSource(repository);
        }

        return rawList;
    }

    public CompleteVersion getCompleteVersion(Version version) throws JsonSyntaxException, IOException {
        CompleteVersion complete = super.getCompleteVersion(version);
        complete.setSource(repository);
        return complete;
    }

    public boolean hasAllFiles(CompleteVersion paramCompleteVersion, OS paramOperatingSystem) {
        return true;
    }

    protected InputStreamReader getUrl(String uri) throws IOException {
        return repository.read(uri);
    }
}
