package net.minecraft.launcher.updater;

import com.turikhay.tlauncher.repository.Repository;

public class OfficialVersionList extends RepositoryBasedVersionList {
	public OfficialVersionList() {
		super(Repository.OFFICIAL_VERSION_REPO);
	}
}
