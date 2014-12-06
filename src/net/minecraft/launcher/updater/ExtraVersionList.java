package net.minecraft.launcher.updater;

import ru.turikhay.tlauncher.repository.Repository;

public class ExtraVersionList extends RepositoryBasedVersionList {

	public ExtraVersionList() {
		super(Repository.EXTRA_VERSION_REPO);
	}

	/*@Override
	public CompleteVersion getCompleteVersion(Version version) throws JsonSyntaxException, IOException {
		if (version instanceof CompleteVersion)
			return (CompleteVersion) version;

		if (version == null)
			throw new NullPointerException("Version cannot be NULL!");

		CompleteVersion complete = gson.fromJson(getUrl("versions/"+ version.getID() +".json"), CompleteVersion.class);

		complete.setID(version.getID()); // IDs should be the same
		complete.setVersionList(this);

		Collections.replaceAll(this.versions, version, complete);

		return complete;
	}*/
}
