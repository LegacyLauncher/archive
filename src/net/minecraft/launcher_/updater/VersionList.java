package net.minecraft.launcher_.updater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.launcher_.OperatingSystem;
import net.minecraft.launcher_.updater.versions.json.DateTypeAdapter;
import net.minecraft.launcher_.updater.versions.json.LowerCaseEnumTypeAdapterFactory;
import net.minecraft.launcher_.versions.CompleteVersion;
import net.minecraft.launcher_.versions.PartialVersion;
import net.minecraft.launcher_.versions.ReleaseType;
import net.minecraft.launcher_.versions.Version;

public abstract class VersionList
{
  protected final Gson gson;
  private final Map<String, Version> versionsByName = new HashMap<String, Version>();
  private final List<Version> versions = new ArrayList<Version>();
  private final Map<ReleaseType, Version> latestVersions = new EnumMap<ReleaseType, Version>(ReleaseType.class);

  public VersionList() {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
    builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
    builder.enableComplexMapKeySerialization();
    builder.setPrettyPrinting();

    this.gson = builder.create();
  }

  public Collection<Version> getVersions() {
    return this.versions;
  }

  public Version getLatestVersion(ReleaseType type) {
    if (type == null) throw new IllegalArgumentException("Type cannot be null");
    return (Version)this.latestVersions.get(type);
  }

  public Version getVersion(String name) {
    if ((name == null) || (name.length() == 0)) throw new IllegalArgumentException("Name cannot be null or empty");
    return (Version)this.versionsByName.get(name);
  }

  public CompleteVersion getCompleteVersion(String name) throws IOException {
    if ((name == null) || (name.length() == 0)) throw new IllegalArgumentException("Name cannot be null or empty");
    Version version = getVersion(name);
    if (version == null) throw new IllegalArgumentException("Unknown version - cannot get complete version of null");
    return getCompleteVersion(version);
  }

  public CompleteVersion getCompleteVersion(Version version) throws IOException {
    if ((version instanceof CompleteVersion)) return (CompleteVersion)version;
    if (version == null) throw new IllegalArgumentException("Version cannot be null");

    CompleteVersion complete = (CompleteVersion)this.gson.fromJson(getUrl("versions/" + version.getId() + "/" + version.getId() + ".json"), CompleteVersion.class);
    ReleaseType type = version.getType();

    Collections.replaceAll(this.versions, version, complete);
    this.versionsByName.put(version.getId(), complete);

    if (this.latestVersions.get(type) == version) {
      this.latestVersions.put(type, complete);
    }

    return complete;
  }

  protected void clearCache() {
    this.versionsByName.clear();
    this.versions.clear();
    this.latestVersions.clear();
  }

  public void refreshVersions() throws IOException {
    clearCache();

    RawVersionList versionList = (RawVersionList)this.gson.fromJson(getUrl("versions/versions.json"), RawVersionList.class);

    for (Version version : versionList.getVersions()) {
      this.versions.add(version);
      this.versionsByName.put(version.getId(), version);
    }

    for (ReleaseType type : ReleaseType.values())
      this.latestVersions.put(type, this.versionsByName.get(versionList.getLatestVersions().get(type)));
  }

  public CompleteVersion addVersion(CompleteVersion version)
  {
    if (version.getId() == null) throw new IllegalArgumentException("Cannot add blank version");
    if (getVersion(version.getId()) != null) throw new IllegalArgumentException("Version '" + version.getId() + "' is already tracked");

    this.versions.add(version);
    this.versionsByName.put(version.getId(), version);

    return version;
  }

  public void removeVersion(String name) {
    if ((name == null) || (name.length() == 0)) throw new IllegalArgumentException("Name cannot be null or empty");
    Version version = getVersion(name);
    if (version == null) throw new IllegalArgumentException("Unknown version - cannot remove null");
    removeVersion(version);
  }

  public void removeVersion(Version version) {
    if (version == null) throw new IllegalArgumentException("Cannot remove null version");
    this.versions.remove(version);
    this.versionsByName.remove(version.getId());

    for (ReleaseType type : ReleaseType.values())
      if (getLatestVersion(type) == version)
        this.latestVersions.remove(type);
  }

  public void setLatestVersion(Version version)
  {
    if (version == null) throw new IllegalArgumentException("Cannot set latest version to null");
    this.latestVersions.put(version.getType(), version);
  }

  public void setLatestVersion(String name) {
    if ((name == null) || (name.length() == 0)) throw new IllegalArgumentException("Name cannot be null or empty");
    Version version = getVersion(name);
    if (version == null) throw new IllegalArgumentException("Unknown version - cannot set latest version to null");
    setLatestVersion(version);
  }

  public String serializeVersionList() {
    RawVersionList list = new RawVersionList();

    for (ReleaseType type : ReleaseType.values()) {
      Version latest = getLatestVersion(type);
      if (latest != null) {
        list.getLatestVersions().put(type, latest.getId());
      }
    }

    for (Version version : getVersions()) {
      PartialVersion partial = null;

      if ((version instanceof PartialVersion))
        partial = (PartialVersion)version;
      else {
        partial = new PartialVersion(version);
      }

      list.getVersions().add(partial);
    }

    return this.gson.toJson(list);
  }

  public String serializeVersion(CompleteVersion version) {
    if (version == null) throw new IllegalArgumentException("Cannot serialize null!");
    return this.gson.toJson(version);
  }

  public abstract boolean hasAllFiles(CompleteVersion paramCompleteVersion, OperatingSystem paramOperatingSystem);

  protected abstract String getUrl(String paramString) throws IOException;

  private static class RawVersionList {
    private List<PartialVersion> versions = new ArrayList<PartialVersion>();
    private Map<ReleaseType, String> latest = new EnumMap<ReleaseType, String>(ReleaseType.class);

    public List<PartialVersion> getVersions() {
      return this.versions;
    }

    public Map<ReleaseType, String> getLatestVersions() {
      return this.latest;
    }
  }
}
