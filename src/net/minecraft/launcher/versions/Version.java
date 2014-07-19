package net.minecraft.launcher.versions;

import java.util.Date;

import ru.turikhay.tlauncher.adapter.PublicCloneable;
import ru.turikhay.tlauncher.repository.Repository;
import net.minecraft.launcher.updater.VersionList;

public interface Version extends PublicCloneable {
	public String getID();

	public void setID(String id);

	public ReleaseType getReleaseType();

	public Repository getSource();

	public void setSource(Repository repository);

	public Date getUpdatedTime();

	public Date getReleaseTime();

	public VersionList getVersionList();

	public void setVersionList(VersionList list);
}
