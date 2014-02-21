package net.minecraft.launcher.updater;

import java.io.IOException;

import com.turikhay.tlauncher.minecraft.repository.VersionRepository;

import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Version;

public class RepositoryBasedVersionList extends RemoteVersionList {
	private final VersionRepository repository;
	
	RepositoryBasedVersionList(VersionRepository repository){
		if(repository == null)
			throw new NullPointerException();
		
		this.repository = repository;
	}
	
	@Override
	public RawVersionList getRawList() throws IOException {
		RawVersionList rawList = super.getRawList();
		
		for(Version version : rawList.getVersions())
			version.setSource(repository);
		
		return rawList;
	}

	@Override
	public boolean hasAllFiles(CompleteVersion paramCompleteVersion, OperatingSystem paramOperatingSystem) {
		return true;
	}

	@Override
	protected String getUrl(String uri) throws IOException {
		return repository.getUrl(uri);
	}

}
