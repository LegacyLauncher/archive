package net.minecraft.launcher.versions;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import net.minecraft.launcher.updater.*;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;
import ru.turikhay.util.json.ExposeExclusion;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompleteVersion implements Version, Cloneable {
    String id;
    String jar;
    String url;
    String family;
    String inheritsFrom;
    Date time;
    Date releaseTime;
    ReleaseType type;
    String jvmArguments;
    String minecraftArguments;
    String mainClass;
    Integer minimumLauncherVersion = Integer.valueOf(0);
    Integer tlauncherVersion = Integer.valueOf(0);
    List<Library> libraries;
    List<Rule> rules;
    List<String> deleteEntries;

    Map<DownloadType, DownloadInfo> downloads = new HashMap<DownloadType, DownloadInfo>();
    AssetIndexInfo assetIndex;
    String assets;

    @Expose Repository source;
    @Expose boolean elyfied;
    @Expose VersionList list;

    public String getID() {
        return id;
    }

    public void setID(String id) {
        if (id != null && !id.isEmpty()) {
            this.id = id;
        } else {
            throw new IllegalArgumentException("ID is NULL or empty");
        }
    }

    public String getUrl() {
        return url;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public ReleaseType getReleaseType() {
        return type;
    }

    public Repository getSource() {
        return source;
    }

    public void setSource(Repository repository) {
        if (repository == null) {
            throw new NullPointerException();
        } else {
            source = repository;
        }
    }

    public Date getUpdatedTime() {
        return time;
    }

    public void setUpdatedTime(Date time) {
        if (time == null) {
            throw new NullPointerException("Time is NULL!");
        } else {
            this.time = time;
        }
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public VersionList getVersionList() {
        return list;
    }

    public void setVersionList(VersionList list) {
        if (list == null) {
            throw new NullPointerException("VersionList cannot be NULL!");
        } else {
            this.list = list;
        }
    }

    public String getJar() {
        return jar;
    }

    public String getInheritsFrom() {
        return inheritsFrom;
    }

    public String getJVMArguments() {
        return jvmArguments;
    }

    public String getMinecraftArguments() {
        return minecraftArguments;
    }

    public void setMinecraftArguments(String args) {
        minecraftArguments = args;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String clazz) {
        mainClass = clazz;
    }

    public List<Library> getLibraries() {
        return libraries;
    }

    public List<Rule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    public List<String> getDeleteEntries() {
        return deleteEntries;
    }

    public int getMinimumLauncherVersion() {
        return minimumLauncherVersion.intValue();
    }

    public int getMinimumCustomLauncherVersion() {
        return tlauncherVersion.intValue();
    }

    public AssetIndexInfo getAssetIndex() {
        if (assetIndex == null) {
            if(assets == null) {
                assetIndex = new AssetIndexInfo("legacy");
            } else {
                assetIndex = new AssetIndexInfo(assets);
            }
        }
        return assetIndex;
    }

    public DownloadInfo getDownloadURL(DownloadType type) {
        return downloads.get(type);
    }

    public boolean isElyfied() {
        return elyfied;
    }

    public void setElyfied(boolean elyfied) {
        this.elyfied = elyfied;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null) {
            return false;
        } else if (hashCode() == o.hashCode()) {
            return true;
        } else if (!(o instanceof Version)) {
            return false;
        } else {
            Version compare = (Version) o;
            return compare.getID() == null ? false : compare.getID().equals(id);
        }
    }

    public String toString() {
        return getClass().getSimpleName() + debugString();
    }

    public String debugString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("jar", jar)
                .append("inheritsFrom", inheritsFrom)
                .append("url", url)
                .append("time", time)
                .append("releaseTime", releaseTime)
                .append("downloads", downloads)
                .append("assetIndex", assetIndex)
                .append("source", source)
                .append("list", list)
                .append("libraries", libraries == null ? null : "(" + libraries.size() + " items)")
                .build();
        //return "{id=\'" + id + "\', jar=" + jar + ", inheritsFrom=" + inheritsFrom + ", time=" + time + ", release=" + releaseTime + ", type=" + type + ", url=" + url + ", downloads= " + downloads + ", class=" + mainClass + ", minimumVersion=" + minimumLauncherVersion + ", assetIndex=\'" + assetIndex + "\', source=" + source + ", list=" + list + ", libraries=" + (libraries == null ? null : "(" + libraries.size() + " items") + "}";
    }

    public File getFile(File base) {
        return new File(base, "versions/" + getID() + "/" + getID() + ".jar");
    }

    public boolean appliesToCurrentEnvironment() {
        if (rules == null) {
            return true;
        } else {
            Iterator var2 = rules.iterator();

            while (var2.hasNext()) {
                Rule rule = (Rule) var2.next();
                Rule.Action action = rule.getAppliedAction();
                if (action == Rule.Action.DISALLOW) {
                    return false;
                }
            }

            return true;
        }
    }

    public Collection<Library> getRelevantLibraries() {
        ArrayList result = new ArrayList();
        Iterator var3 = libraries.iterator();

        while (var3.hasNext()) {
            Library library = (Library) var3.next();
            if (library.appliesToCurrentEnvironment()) {
                result.add(library);
            }
        }

        return result;
    }

    public Collection<File> getClassPath(OS os, File base) {
        Collection libraries = getRelevantLibraries();
        ArrayList result = new ArrayList();
        Iterator var6 = libraries.iterator();

        while (var6.hasNext()) {
            Library library = (Library) var6.next();
            if (library.getNatives() == null) {
                result.add(new File(base, "libraries/" + library.getArtifactPath()));
            }
        }

        result.add(new File(base, "versions/" + getID() + "/" + getID() + ".jar"));
        return result;
    }

    public Collection<File> getClassPath(File base) {
        return getClassPath(OS.CURRENT, base);
    }

    public Collection<String> getNatives(OS os) {
        Collection libraries = getRelevantLibraries();
        ArrayList result = new ArrayList();
        Iterator var5 = libraries.iterator();

        while (var5.hasNext()) {
            Library library = (Library) var5.next();
            Map natives = library.getNatives();
            if (natives != null && natives.containsKey(os)) {
                result.add("libraries/" + library.getArtifactPath((String) natives.get(os)));
            }
        }

        return result;
    }

    public Collection<String> getNatives() {
        return getNatives(OS.CURRENT);
    }

    public Set<String> getRequiredFiles(OS os) {
        HashSet neededFiles = new HashSet();
        Iterator var4 = getRelevantLibraries().iterator();

        while (var4.hasNext()) {
            Library library = (Library) var4.next();
            if (library.getNatives() != null) {
                String natives = library.getNatives().get(os);
                if (natives != null) {
                    neededFiles.add("libraries/" + library.getArtifactPath(natives));
                }
            } else {
                neededFiles.add("libraries/" + library.getArtifactPath());
            }
        }

        return neededFiles;
    }

    public Collection<String> getExtractFiles(OS os) {
        Collection libraries = getRelevantLibraries();
        ArrayList result = new ArrayList();
        Iterator var5 = libraries.iterator();

        while (var5.hasNext()) {
            Library library = (Library) var5.next();
            Map natives = library.getNatives();
            if (natives != null && natives.containsKey(os)) {
                result.add("libraries/" + library.getArtifactPath((String) natives.get(os)));
            }
        }

        return result;
    }

    public CompleteVersion resolve(VersionManager vm) throws IOException {
        return resolve(vm, false);
    }

    public CompleteVersion resolve(VersionManager vm, boolean useLatest) throws IOException {
        return resolve(vm, useLatest, new ArrayList());
    }

    public static final String FORGE_PREFIX = "Forge-";

    protected CompleteVersion resolve(VersionManager vm, boolean useLatest, List<String> inheristance) throws IOException {
        if (vm == null) {
            throw new NullPointerException("version manager");
        } else if (inheritsFrom == null) {
            if (family == null || family.equals(FORGE_PREFIX)) {
                String family_;

                switch (type) {
                    case UNKNOWN:
                    case OLD_ALPHA:
                    case SNAPSHOT:
                        family_ = type.toString();
                        break;
                    default:
                        family_ = getFamilyOf(id);
                        if (family_ == null && jar != null)
                            family_ = getFamilyOf(jar);
                        if (family_ == null && inheritsFrom != null)
                            family_ = getFamilyOf(inheritsFrom);
                }

                if (family_ == null)
                    family_ = "unknown";

                family = family_;
            }
            return this;
        } else if (inheristance.contains(id)) {
            throw new CompleteVersion.DuplicateInheritanceException();
        } else {
            inheristance.add(id);
            VersionSyncInfo parentSyncInfo = vm.getVersionSyncInfo(inheritsFrom);
            if (parentSyncInfo == null) {
                throw new CompleteVersion.ParentNotFoundException();
            } else {
                CompleteVersion result;

                try {
                    result = (CompleteVersion) parentSyncInfo.getCompleteVersion(useLatest).resolve(vm, useLatest, inheristance).clone();
                } catch (CloneNotSupportedException var7) {
                    throw new RuntimeException(var7);
                }

                log(result);
                log(copyInto(result));

                if (id.toLowerCase().contains("forge") && family == null && result.family != null && !result.family.startsWith(FORGE_PREFIX)) {
                    family = FORGE_PREFIX + result.family;
                }

                return copyInto(result);
            }
        }
    }

    public CompleteVersion copyInto(CompleteVersion result) {
        result.id = id;

        if (jar != null) {
            result.jar = jar;
        }

        if (family != null) {
            result.family = family;
        }

        result.inheritsFrom = null;

        if (time.getTime() != 0L) {
            result.time = time;
        }

        if (type != ReleaseType.UNKNOWN) {
            result.type = type;
        }

        if (jvmArguments != null) {
            result.jvmArguments = jvmArguments;
        }

        if (minecraftArguments != null) {
            result.minecraftArguments = minecraftArguments;
        }

        if (mainClass != null) {
            result.mainClass = mainClass;
        }

        ArrayList rulesCopy;
        if (libraries != null) {
            rulesCopy = new ArrayList();
            rulesCopy.addAll(libraries);
            if (result.libraries != null) {
                rulesCopy.addAll(result.libraries);
            }

            result.libraries = rulesCopy;
        }

        if (rules != null) {
            if (result.rules != null) {
                result.rules.addAll(rules);
            } else {
                rulesCopy = new ArrayList(rules.size());
                Collections.copy(rulesCopy, rules);
                result.rules = rules;
            }
        }

        if (deleteEntries != null) {
            if (result.deleteEntries != null) {
                result.deleteEntries.addAll(deleteEntries);
            } else {
                result.deleteEntries = new ArrayList(deleteEntries);
            }
        }

        if (minimumLauncherVersion != 0) {
            result.minimumLauncherVersion = minimumLauncherVersion;
        }

        if (tlauncherVersion != 0) {
            result.tlauncherVersion = tlauncherVersion;
        }

        if (assetIndex != null) {
            result.assetIndex = assetIndex;
        }

        if (downloads != null && downloads.size() > 0) {
            result.downloads = downloads;
        }

        if (source != null) {
            result.source = source;
        }

        result.list = list;
        return result;
    }

    private void log(Object... o) {
        U.log("[Version:" + id + "]", o);
    }

    protected static final Pattern familyPattern = Pattern.compile("([a-z]*[\\d]\\.[\\d]+).*");

    private static String getFamilyOf(String id) {
        String family;

        Matcher matcher = familyPattern.matcher(id);
        if (matcher.matches()) {
            family = matcher.group(1);
        } else {
            family = null;
        }

        return family;
    }

    public static class CompleteVersionSerializer implements JsonSerializer<CompleteVersion>, JsonDeserializer<CompleteVersion> {
        private final Gson defaultContext;

        public CompleteVersionSerializer() {
            GsonBuilder builder = new GsonBuilder();
            ExposeExclusion.setup(builder);
            builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
            builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
            builder.enableComplexMapKeySerialization();
            builder.setPrettyPrinting();
            defaultContext = builder.create();
        }

        public CompleteVersion deserialize(JsonElement elem, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = elem.getAsJsonObject();
            JsonElement originalId = object.get("original_id");
            if (originalId != null && originalId.isJsonPrimitive()) {
                String unnecessaryEntries = originalId.getAsString();
                object.remove("original_id");
                object.addProperty("jar", unnecessaryEntries);
            }

            JsonElement unnecessaryEntries1 = object.get("unnecessaryEntries");
            if (unnecessaryEntries1 != null && unnecessaryEntries1.isJsonArray()) {
                object.remove("unnecessaryEntries");
                object.add("deleteEntries", unnecessaryEntries1);
            }

            CompleteVersion version = defaultContext.fromJson(elem, CompleteVersion.class);
            if (version.id == null) {
                throw new JsonParseException("Version ID is NULL!");
            } else {
                if (version.type == null) {
                    version.type = ReleaseType.UNKNOWN;
                }

                if (version.source == null) {
                    version.source = Repository.LOCAL_VERSION_REPO;
                }

                if (version.time == null) {
                    version.time = new Date(0L);
                }

                return version;
            }
        }

        public JsonElement serialize(CompleteVersion version0, Type type, JsonSerializationContext context) {
            CompleteVersion version;
            try {
                version = (CompleteVersion) version0.clone();
            } catch (CloneNotSupportedException var7) {
                U.log("Cloning of CompleteVersion is not supported O_o", var7);
                return defaultContext.toJsonTree(version0, type);
            }

            JsonObject object = (JsonObject) defaultContext.toJsonTree(version, type);
            JsonElement jar = object.get("jar");
            if (jar == null) {
                object.remove("downloadJarLibraries");
            }

            return object;
        }
    }

    public class DuplicateInheritanceException extends CompleteVersion.InheritanceException {
        public DuplicateInheritanceException() {
        }
    }

    public class InheritanceException extends IOException {
        InheritanceException() {
            super(id + " should inherit from " + inheritsFrom);
        }

        public String getID() {
            return id;
        }

        public String getInheritsFrom() {
            return inheritsFrom;
        }

        public CompleteVersion getVersion() {
            return CompleteVersion.this;
        }
    }

    public class ParentNotFoundException extends CompleteVersion.InheritanceException {
        public ParentNotFoundException() {
        }
    }
}
