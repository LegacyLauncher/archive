package net.minecraft.launcher.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import net.minecraft.launcher.versions.CompleteVersion;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;

public class LocalVersionList extends StreamVersionList {

	private File baseDirectory;
	private File baseVersionsDir;

	public LocalVersionList(File baseDirectory) throws IOException {
		this.setBaseDirectory(baseDirectory);
	}

	public File getBaseDirectory() {
		return this.baseDirectory;
	}

	public void setBaseDirectory(File directory) throws IOException {
		if (directory == null)
			throw new IllegalArgumentException("Base directory is NULL!");

		FileUtil.createFolder(directory);
		log("Base directory:", directory.getAbsolutePath());

		this.baseDirectory = directory;
		this.baseVersionsDir = new File(this.baseDirectory, "versions");
	}

	@Override
	public void refreshVersions() throws IOException {
		clearCache();

		File[] files = this.baseVersionsDir.listFiles();
		if (files == null)
			return;

		for (File directory : files) {
			String id = directory.getName();
			File jsonFile = new File(directory, id + ".json");

			if (!directory.isDirectory() || !jsonFile.isFile())
				continue;

			try {
				CompleteVersion version = this.gson.fromJson(getUrl("versions/"
						+ id + "/" + id + ".json"), CompleteVersion.class);

				if (version == null) {
					log("JSON descriptor of version \"" + id
							+ "\" in NULL, it won't be added in list as local.");
					continue;
				}

				version.setID(id);
				version.setSource(Repository.LOCAL_VERSION_REPO);
				version.setVersionList(this);

				addVersion(version);
			} catch (Exception ex) {
				log("Error occurred while parsing local version", id, ex);
			}
		}
	}

	public void saveVersion(CompleteVersion version) throws IOException {
		String text = serializeVersion(version);
		File target = new File(this.baseVersionsDir, version.getID() + "/"
				+ version.getID() + ".json");

		FileUtil.writeFile(target, text);
	}
	
	public void deleteVersion(String id, boolean deleteLibraries) throws IOException {
		CompleteVersion version = getCompleteVersion(id);
		
		if(version == null)
			throw new IllegalArgumentException("Version is not installed!");
		
		File dir = new File(this.baseVersionsDir, id + '/');
		
		if(!dir.isDirectory())
			throw new IOException("Cannot find directory: "+ dir.getAbsolutePath());
		
		FileUtil.deleteDirectory(dir);
		
		if(!deleteLibraries) return;
		
		for(File library : version.getClassPath(baseDirectory))
			FileUtil.deleteFile(library);
		
		for(String nativeLib : version.getNatives())
			FileUtil.deleteFile(new File(baseDirectory, nativeLib));
	}
	
	@Override
	protected InputStream getInputStream(String uri) throws IOException {
		return new FileInputStream(new File(this.baseDirectory, uri));
	}

	@Override
	public boolean hasAllFiles(CompleteVersion version, OS os) {
		Set<String> files = version.getRequiredFiles(os);

		for (String file : files) {
			File required = new File(this.baseDirectory, file);
			if (!required.isFile() || required.length() == 0L)
				return false;
		}

		return true;
	}

}
