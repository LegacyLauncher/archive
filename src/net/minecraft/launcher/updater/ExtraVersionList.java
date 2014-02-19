package net.minecraft.launcher.updater;

import com.turikhay.tlauncher.minecraft.repository.VersionRepository;

public class ExtraVersionList extends RepositoryBasedVersionList {

	public ExtraVersionList() {
		super(VersionRepository.EXTRA);
	}
}
