package net.minecraft.launcher.updater;

import com.google.gson.JsonSyntaxException;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Version;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.UrlEncoder;

import java.io.IOException;
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
            CompleteVersion complete = gson.fromJson(getUrl("versions/" + UrlEncoder.encode(version.getID(), true) + ".json"), CompleteVersion.class);
            complete.setID(version.getID());
            complete.setVersionList(this);
            complete.setSource(Repository.EXTRA_VERSION_REPO);
            Collections.replaceAll(versions, version, complete);
            return complete;
        }
    }
}
