package net.minecraft.launcher.updater;

import com.google.gson.JsonSyntaxException;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.PartialVersion;
import net.minecraft.launcher.versions.Version;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.OS;

import java.io.IOException;
import java.io.InputStreamReader;

public class RepositoryBasedVersionList extends RemoteVersionList {
    private final Repository repository;

    RepositoryBasedVersionList(Repository repository) {
        if (repository == null) {
            throw new NullPointerException();
        } else {
            this.repository = repository;
        }
    }

    public RawVersionList getRawList() throws IOException {
        RawVersionList rawList = super.getRawList();

        for (PartialVersion version : rawList.getVersions()) {
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
