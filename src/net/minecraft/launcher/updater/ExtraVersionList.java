package net.minecraft.launcher.updater;

import com.turikhay.tlauncher.repository.Repository;

public class ExtraVersionList extends RepositoryBasedVersionList {

	public ExtraVersionList() {
		super(Repository.EXTRA_VERSION_REPO);
	}
}
