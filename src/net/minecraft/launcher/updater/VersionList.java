package net.minecraft.launcher.updater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ru.turikhay.util.OS;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.PartialVersion;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.CompleteVersion.CompleteVersionSerializer;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import net.minecraft.launcher.versions.Version;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public abstract class VersionList {
	final Gson gson;
	private final Map<String, Version> byName;
	private final List<Version> versions;
	private final Map<ReleaseType, Version> latest;

	VersionList() {
		this.versions = new ArrayList<Version>();
		this.byName = new Hashtable<String, Version>();
		this.latest = new Hashtable<ReleaseType, Version>();

		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
		builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
		builder.registerTypeAdapter(CompleteVersion.class,
				new CompleteVersionSerializer());
		builder.enableComplexMapKeySerialization();
		builder.setPrettyPrinting();

		this.gson = builder.create();
	}

	public List<Version> getVersions() {
		synchronized (versions) {
			return Collections.unmodifiableList(versions);
		}
	}

	public Map<ReleaseType, Version> getLatestVersions() {
		return Collections.unmodifiableMap(latest);
	}

	public Version getVersion(String name) {
		if (name == null || name.isEmpty())
			throw new IllegalArgumentException("Name cannot be NULL or empty");

		return byName.get(name);
	}

	public CompleteVersion getCompleteVersion(Version version)
			throws JsonSyntaxException, IOException {
		if (version instanceof CompleteVersion)
			return (CompleteVersion) version;

		if (version == null)
			throw new NullPointerException("Version cannot be NULL!");

		CompleteVersion complete = gson.fromJson(
				getUrl("versions/" + version.getID() + "/" + version.getID()
						+ ".json"), CompleteVersion.class);

		complete.setID(version.getID()); // IDs should be the same
		complete.setVersionList(this);

		Collections.replaceAll(this.versions, version, complete);

		return complete;
	}

	public CompleteVersion getCompleteVersion(String name)
			throws JsonSyntaxException, IOException {
		Version version = getVersion(name);
		if (version == null)
			return null;

		return getCompleteVersion(version);
	}

	public Version getLatestVersion(ReleaseType type) {
		if (type == null)
			throw new NullPointerException();

		return latest.get(type);
	}

	public RawVersionList getRawList() throws IOException {
		Object lock = new Object();
		Time.start(lock);

		RawVersionList list = this.gson.fromJson(
				getUrl("versions/versions.json"), RawVersionList.class);

		for (PartialVersion version : list.versions)
			version.setVersionList(this);

		log("Got in", Time.stop(lock), "ms");

		return list;
	}

	public void refreshVersions(RawVersionList versionList) {
		clearCache();

		for (Version version : versionList.getVersions()) {
			this.versions.add(version);
			this.byName.put(version.getID(), version);
		}

		for (Entry<ReleaseType, String> en : versionList.latest.entrySet()) {
			ReleaseType releaseType = en.getKey();

			if (releaseType == null) {
				log("Unknown release type for latest version entry:", en);
				continue;
			}

			Version version = getVersion(en.getValue());

			if (version == null)
				throw new NullPointerException(
						"Cannot find version for latest version entry: " + en);

			this.latest.put(releaseType, version);
		}
	}

	public void refreshVersions() throws IOException {
		refreshVersions(getRawList());
	}

	CompleteVersion addVersion(CompleteVersion version) {
		if (version.getID() == null)
			throw new IllegalArgumentException("Cannot add blank version");

		if (getVersion(version.getID()) != null) {
			log("Version '" + version.getID() + "' is already tracked");
			return version;
		}

		versions.add(version);
		byName.put(version.getID(), version);

		return version;
	}

	void removeVersion(Version version) {
		if (version == null)
			throw new NullPointerException("Version cannot be NULL!");

		versions.remove(version);
		byName.remove(version);
	}

	public void removeVersion(String name) {
		Version version = getVersion(name);
		if (version == null)
			return;
		removeVersion(version);
	}

	public String serializeVersion(CompleteVersion version) {
		if (version == null)
			throw new NullPointerException("CompleteVersion cannot be NULL!");

		return gson.toJson(version);
	}

	//
	public abstract boolean hasAllFiles(CompleteVersion paramCompleteVersion,
			OS paramOperatingSystem);

	protected abstract String getUrl(String uri) throws IOException;

	//

	void clearCache() {
		byName.clear();
		versions.clear();
		latest.clear();
	}

	void log(Object... obj) {
		U.log("[" + getClass().getSimpleName() + "]", obj);
	}

	public static class RawVersionList {
		List<PartialVersion> versions = new ArrayList<PartialVersion>();
		Map<ReleaseType, String> latest = new EnumMap<ReleaseType, String>(
				ReleaseType.class);

		public List<PartialVersion> getVersions() {
			return this.versions;
		}

		public Map<ReleaseType, String> getLatestVersions() {
			return this.latest;
		}
	}
}
