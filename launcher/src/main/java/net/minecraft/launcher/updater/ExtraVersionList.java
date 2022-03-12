package net.minecraft.launcher.updater;

import com.google.gson.JsonSyntaxException;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Version;
import ru.turikhay.tlauncher.repository.Repository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;

public class ExtraVersionList extends RepositoryBasedVersionList {
    public ExtraVersionList() {
        super(Repository.EXTRA_VERSION_REPO);
    }

    public CompleteVersion getCompleteVersion(Version version) throws JsonSyntaxException, IOException {
        if (version instanceof CompleteVersion) {
            return (CompleteVersion) version;
        } else if (version == null) {
            throw new NullPointerException("Version cannot be NULL!");
        } else {
            CompleteVersion complete;
            try (InputStreamReader reader = getUrl("versions/" + version.getID() + ".json")) {
                complete = gson.fromJson(reader, CompleteVersion.class);
            }
            complete.setID(version.getID());
            complete.setVersionList(this);
            complete.setSource(Repository.EXTRA_VERSION_REPO);
            Collections.replaceAll(versions, version, complete);
            return complete;
        }
    }
}
