package ru.turikhay.tlauncher.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.component.ComponentDependence;
import ru.turikhay.tlauncher.component.LauncherComponent;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.DownloadableContainer;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;

import net.minecraft.launcher.updater.AssetIndex;
import net.minecraft.launcher.updater.AssetIndex.AssetObject;
import net.minecraft.launcher.versions.CompleteVersion;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@ComponentDependence({ VersionManager.class, VersionLists.class })
public class AssetsManager extends LauncherComponent {
	private final Gson gson;
	private final Object assetsFlushLock;

	public AssetsManager(ComponentManager manager) throws Exception {
		super(manager);

		this.gson = TLauncher.getGson();
		this.assetsFlushLock = new Object();
	}

	public DownloadableContainer downloadResources(CompleteVersion version,
			List<AssetObject> list, boolean force) throws IOException {
		File baseDirectory = manager.getLauncher().getVersionManager()
				.getLocalList().getBaseDirectory();
		DownloadableContainer container = new DownloadableContainer();

		container.addAll(getResourceFiles(version, baseDirectory, list));

		return container;
	}

	private Set<Downloadable> getResourceFiles(CompleteVersion version,
			File baseDirectory, List<AssetObject> list) {
		Set<Downloadable> result = new HashSet<Downloadable>();

		File objectsFolder = new File(baseDirectory, "assets/objects");

		for (AssetObject object : list) {
			String filename = object.getFilename();
			Downloadable d = new Downloadable(Repository.ASSETS_REPO, filename,
					new File(objectsFolder, filename), false, true);
			result.add(d);
		}

		return result;
	}

	List<AssetObject> getResourceFiles(CompleteVersion version,
			File baseDirectory, boolean local) {
		List<AssetObject> list = null;

		if (!local)
			try {
				list = getRemoteResourceFilesList(version, baseDirectory, true);
			} catch (Exception e) {
				log("Cannot get remote assets list. Trying to use the local one.",
						e);
			}

		if (list == null)
			list = getLocalResourceFilesList(version, baseDirectory);

		if (list == null)
			try {
				list = getRemoteResourceFilesList(version, baseDirectory, true);
			} catch (Exception e) {
				log("Gave up trying to get assets list.", e);
			}

		return list;
	}

	private List<AssetObject> getLocalResourceFilesList(
			CompleteVersion version, File baseDirectory) {
		List<AssetObject> result = new ArrayList<AssetObject>();

		String indexName = version.getAssets();

		File indexesFolder = new File(baseDirectory, "assets/indexes/");
		File indexFile = new File(indexesFolder, indexName + ".json");

		log("Reading indexes from file", indexFile);

		String json;
		try {
			json = FileUtil.readFile(indexFile);
		} catch (Exception e) {
			log("Cannot read local resource files list for index:", indexName,
					e);
			return null;
		}

		AssetIndex index = null;

		try {
			index = this.gson.fromJson(json, AssetIndex.class);
		} catch (JsonSyntaxException e) {
			log("JSON file is invalid", e);
		}

		if (index == null) {
			log("Cannot read data from JSON file.");
			return null;
		}

		for (AssetObject object : index.getUniqueObjects())
			result.add(object);

		return result;
	}

	private List<AssetObject> getRemoteResourceFilesList(
			CompleteVersion version, File baseDirectory, boolean save)
			throws IOException {
		List<AssetObject> result = new ArrayList<AssetObject>();

		String indexName = version.getAssets();
		if (indexName == null)
			indexName = "legacy";

		File assets = new File(baseDirectory, "assets");
		File indexesFolder = new File(assets, "indexes");

		File indexFile = new File(indexesFolder, indexName + ".json");
		
		log("Reading from repository...");

		String json = Repository.OFFICIAL_VERSION_REPO.getUrl("indexes/"
				+ indexName + ".json");
		if (save)
			synchronized (assetsFlushLock) {
				FileUtil.writeFile(indexFile, json);
			}

		AssetIndex index = this.gson.fromJson(json, AssetIndex.class);

		for (AssetObject object : index.getUniqueObjects())
			result.add(object);

		return result;
	}

	List<AssetObject> checkResources(CompleteVersion version,
			File baseDirectory, boolean local, boolean fast) {
		log("Checking resources...");

		List<AssetObject> list, r = new ArrayList<AssetObject>();

		if (local)
			list = getLocalResourceFilesList(version, baseDirectory);
		else
			list = getResourceFiles(version, baseDirectory, true);

		if (list == null) {
			log("Cannot get assets list. Aborting.");
			return r;
		}

		log("Fast comparing:", fast);

		for (AssetObject resource : list)
			if (!checkResource(baseDirectory, resource, fast))
				r.add(resource);

		return r;
	}

	public List<AssetObject> checkResources(CompleteVersion version,
			boolean fast) {
		return checkResources(version, manager.getComponent(VersionLists.class)
				.getLocal().getBaseDirectory(), false, fast);
	}

	private boolean checkResource(File baseDirectory, AssetObject local,
			boolean fast) {
		String path = local.getFilename();

		File file = new File(baseDirectory, "assets/objects/" + path);
		long size = file.length();

		if (!file.isFile() || size == 0L)
			return false;

		if (fast)
			return true;

		// Checking size
		if (local.getSize() != size)
			return false;

		// Checking hash
		if (local.getHash() == null)
			return true;

		return local.getHash().equals(FileUtil.getChecksum(file, "SHA-1"));
	}
}
