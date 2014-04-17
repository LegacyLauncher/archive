package net.minecraft.launcher.updater;

import java.io.IOException;

import com.google.gson.JsonSyntaxException;
import com.turikhay.tlauncher.repository.Repository;
import com.turikhay.util.OS;

import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Version;

public class RepositoryBasedVersionList extends RemoteVersionList {
	private final Repository repository;

	RepositoryBasedVersionList(Repository repository) {
		if (repository == null)
			throw new NullPointerException();

		this.repository = repository;
	}

	@Override
	public RawVersionList getRawList() throws IOException {
		RawVersionList rawList = super.getRawList();

		for (Version version : rawList.getVersions())
			version.setSource(repository);

		return rawList;
	}

	@Override
	public CompleteVersion getCompleteVersion(Version version)
			throws JsonSyntaxException, IOException {
		CompleteVersion complete = super.getCompleteVersion(version);

		complete.setSource(repository);

		return complete;
	}

	@Override
	public boolean hasAllFiles(CompleteVersion paramCompleteVersion,
			OS paramOperatingSystem) {
		return true;
	}

	@Override
	protected String getUrl(String uri) throws IOException {
		return repository.getUrl(uri);
	}

}
