package net.minecraft.launcher.updater;

import com.turikhay.tlauncher.minecraft.repository.VersionRepository;

public class OfficialVersionList extends RepositoryBasedVersionList {
	public OfficialVersionList() {
		super(VersionRepository.OFFICIAL);
	}
}
