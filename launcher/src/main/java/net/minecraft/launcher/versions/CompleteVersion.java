package net.minecraft.launcher.versions;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import net.minecraft.launcher.updater.AssetIndexInfo;
import net.minecraft.launcher.updater.DownloadInfo;
import net.minecraft.launcher.updater.VersionList;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.OS;
import ru.turikhay.util.json.ExposeExclusion;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompleteVersion implements Version, Cloneable {
    private static final Logger LOGGER = LogManager.getLogger();

    String id;
    String jar;
    String url;
    String family;
    String inheritsFrom;
    Date time;
    Date releaseTime;
    String type;
    String jvmArguments;
    String minecraftArguments;
    String mainClass;
    Integer minimumLauncherVersion = 0;
    Integer tlauncherVersion = 0;
    List<Library> libraries;
    List<Rule> rules;
    List<String> deleteEntries;

    Map<String, DownloadInfo> downloads = new HashMap<>();
    AssetIndexInfo assetIndex;
    String assets;

    Map<ArgumentType, List<Argument>> arguments;

    Boolean modListAbsolutePrefix;
    String modpack;

    JavaVersion javaVersion;

    Map<String, LoggingConfiguration> logging;

    @Expose
    Repository source;
    @Expose
    final Set<String> proceededFor = new HashSet<>();
    @Expose
    VersionList list;

    public CompleteVersion() {
    }

    public CompleteVersion(String id, String minecraftArguments, String mainClass, String assets, String jar, Date time, Date releaseTime, Integer minimumLauncherVersion, List<Library> libraries) {
        this.id = id;
        this.minecraftArguments = minecraftArguments;
        this.mainClass = mainClass;
        this.assets = assets;
        this.jar = jar;
        this.time = time;
        this.releaseTime = releaseTime;
        this.minimumLauncherVersion = minimumLauncherVersion;
        this.libraries = libraries;
    }

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
        return ReleaseType.of(type);
    }

    public String getType() {
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

    public ModpackType getModpackType() {
        if (modListAbsolutePrefix != null && modListAbsolutePrefix) // modListAbsolutePrefix can be NULL
            return ModpackType.FORGE_LEGACY_ABSOLUTE; // backwards compatibility

        return ModpackType.getByName(modpack, jar);
    }

    public JavaVersion getJavaVersion() {
        return javaVersion;
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
        return minimumLauncherVersion;
    }

    public int getMinimumCustomLauncherVersion() {
        return tlauncherVersion;
    }

    public AssetIndexInfo getAssetIndex() {
        if (assetIndex == null) {
            if (assets == null) {
                assetIndex = new AssetIndexInfo("legacy");
            } else {
                assetIndex = new AssetIndexInfo(assets);
            }
        }
        return assetIndex;
    }

    public DownloadInfo getDownloadURL(String type) {
        return downloads.get(type);
    }

    public boolean isProceededFor(String type) {
        return proceededFor.contains(type);
    }

    public void setProceededFor(String type) {
        this.proceededFor.add(type);
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
            return compare.getID() != null && compare.getID().equals(id);
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

    public boolean appliesToCurrentEnvironment(Rule.FeatureMatcher matcher) {
        if (rules != null) {
            for (Rule rule : rules) {
                Rule.Action action = rule.getAppliedAction(matcher);
                if (action == Rule.Action.DISALLOW) {
                    return false;
                }
            }

        }
        return true;
    }

    public Collection<Library> getRelevantLibraries(Rule.FeatureMatcher matcher) {
        ArrayList<Library> result = new ArrayList<>();

        for (Library library : libraries) {
            if (library.appliesToCurrentEnvironment(matcher)) {
                result.add(library);
            }
        }

        return result;
    }

    private Stream<Library> streamLibraries(LibraryType type, Rule.FeatureMatcher matcher) {
        return getRelevantLibraries(matcher)
                .stream()
                .filter(lib -> lib.getLibraryType() == type);
    }

    public Collection<File> getClassPath(OS os, Rule.FeatureMatcher matcher, File base) {
        List<File> result = streamLibraries(LibraryType.LIBRARY, matcher)
                .filter(lib -> lib.getNatives() == null)
                .map(lib -> new File(base, "libraries/" + lib.getArtifactPath()))
                .collect(Collectors.toList());

        result.add(getJarFile(base));
        return result;
    }

    public File getJarFile(File base) {
        return new File(base, "versions/" + getID() + "/" + getID() + ".jar");
    }

    public List<Library> getMods(Rule.FeatureMatcher matcher) {
        return streamLibraries(LibraryType.MODIFICATION, matcher)
                .collect(Collectors.toList());
    }

    public List<Library> getTransformers(Rule.FeatureMatcher matcher) {
        return streamLibraries(LibraryType.TRANSFORMER, matcher)
                .collect(Collectors.toList());
    }

    public Collection<File> getClassPath(Rule.FeatureMatcher matcher, File base) {
        return getClassPath(OS.CURRENT, matcher, base);
    }

    public Collection<String> getNatives(OS os, Rule.FeatureMatcher matcher) {
        Collection<Library> libraries = getRelevantLibraries(matcher);
        ArrayList<String> result = new ArrayList<>();

        for (Library library : libraries) {
            Map<OS, String> natives = library.getNatives();
            if (natives != null && natives.containsKey(os)) {
                result.add("libraries/" + library.getArtifactPath(natives.get(os)));
            }
        }

        return result;
    }

    public Collection<String> getNatives(Rule.FeatureMatcher matcher) {
        return getNatives(OS.CURRENT, matcher);
    }

    public Set<String> getRequiredFiles(OS os, Rule.FeatureMatcher matcher) {
        HashSet<String> neededFiles = new HashSet<>();

        for (Library library : getRelevantLibraries(matcher)) {
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

    public Collection<String> getExtractFiles(OS os, Rule.FeatureMatcher matcher) {
        return getRelevantLibraries(matcher)
                .stream()
                .filter(lib -> lib.getNatives() != null)
                .filter(lib -> lib.getNatives().containsKey(os))
                .map(lib -> "libraries/" + lib.getArtifactPath(lib.getNatives().get(os)))
                .collect(Collectors.toList());
    }

    public CompleteVersion resolve(VersionManager vm, boolean useLatest) throws IOException {
        return resolve(vm, useLatest, new ArrayList<>());
    }

    public static final String FORGE_PREFIX = "Forge-";
    public static final String FABRIC_PREFIX = "Fabric-";

    protected void resolveFamily(String parent_family) {
        if (family != null) return; // early exit

        if (parent_family == null) parent_family = "unknown"; // to prevent NPE

        if (parent_family.startsWith(FORGE_PREFIX) || parent_family.startsWith(FABRIC_PREFIX)) {
            family = parent_family;
            return;
        }

        if (id.toLowerCase(java.util.Locale.ROOT).contains("forge")) {
            family = FORGE_PREFIX + parent_family;
            return;
        }
        if (id.toLowerCase(java.util.Locale.ROOT).contains("fabric")) {
            family = FABRIC_PREFIX + parent_family;
            return;
        }

        family = parent_family;
    }

    public CompleteVersion resolve(VersionManager vm, boolean useLatest, List<String> inheritance) throws IOException {
        if (vm == null) {
            throw new NullPointerException("version manager");
        } else if (inheritsFrom == null) {
            if (family == null || family.equals(FORGE_PREFIX) || family.equals(FABRIC_PREFIX)) {
                String family_;

                switch (getReleaseType()) {
                    case UNKNOWN:
                    case OLD_ALPHA:
                    case SNAPSHOT:
                        family_ = type;
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

                resolveFamily(family_);
            }
            return this.clone();
        } else if (inheritance.contains(id)) {
            throw new CompleteVersion.DuplicateInheritanceException();
        } else {
            if (jar == null) {
                jar = inheritsFrom;
            }
            inheritance.add(id);
            VersionSyncInfo parentSyncInfo = vm.getVersionSyncInfo(inheritsFrom, inheritance);
            if (parentSyncInfo == null) {
                throw new CompleteVersion.ParentNotFoundException();
            } else {
                CompleteVersion result = parentSyncInfo.getCompleteVersion(useLatest).resolve(vm, useLatest, inheritance);

                resolveFamily(result.family);

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

        if (result.modListAbsolutePrefix == null || !result.modListAbsolutePrefix) {
            result.modListAbsolutePrefix = modListAbsolutePrefix;
        }

        if ((result.releaseTime == null || result.releaseTime.getTime() == 0L) && releaseTime != null) {
            result.releaseTime = releaseTime;
        }

        if (time != null && time.getTime() != 0L) {
            result.time = time;
        }

        if (type != null) {
            result.type = type;
        }

        if (modpack != null) {
            result.modpack = modpack;
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

        if (libraries != null) {
            Set<Library> lib = new LinkedHashSet<>(libraries);
            if (result.libraries != null) {
                lib.addAll(result.libraries);
            }
            /*rulesCopy = new ArrayList();
            rulesCopy.addAll(libraries);
            if (result.libraries != null) {
                rulesCopy.addAll(result.libraries);
            }*/
            result.libraries = new ArrayList<>();
            result.libraries.addAll(lib);
        }

        if (arguments != null) {
            if (result.arguments == null) result.arguments = new java.util.EnumMap<>(ArgumentType.class);
            for (Map.Entry<ArgumentType, List<Argument>> entry : this.arguments.entrySet()) {
                List<Argument> arguments = result.arguments
                        .computeIfAbsent(entry.getKey(), k -> new ArrayList<>());

                arguments.addAll(entry.getValue());
            }
        }

        if (rules != null) {
            if (result.rules != null) {
                result.rules.addAll(rules);
            } else {
                result.rules = new ArrayList<>(rules);
            }
        }

        if (deleteEntries != null) {
            if (result.deleteEntries != null) {
                result.deleteEntries.addAll(deleteEntries);
            } else {
                result.deleteEntries = new ArrayList<>(deleteEntries);
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

        if (javaVersion != null) {
            result.javaVersion = javaVersion;
        }

        if (source != null) {
            result.source = source;
        }

        if (logging != null) {
            result.logging = logging;
        }

        result.list = list;
        return result;
    }

    public Rule.FeatureMatcher createFeatureMatcher() {
        return new CurrentLaunchFeatureMatcher();
    }

    public boolean hasModernArguments() {
        return this.arguments != null;
    }

    public List<String> addArguments(ArgumentType type, Rule.FeatureMatcher featureMatcher, StrSubstitutor substitutor) {
        ArrayList<String> result = new ArrayList<>();
        if (this.arguments != null) {
            List<Argument> args = this.arguments.get(type);
            if (args != null) {
                for (Argument argument : args) {
                    result.addAll(argument.apply(featureMatcher, substitutor));
                }
            }
        } else {
            if (this.minecraftArguments != null) {
                if (type == ArgumentType.GAME) {
                    for (String arg : this.minecraftArguments.split(" ")) {
                        result.add(substitutor.replace(arg));
                    }
                    if (featureMatcher.hasFeature("is_demo_user", Boolean.TRUE)) {
                        result.add("--demo");
                    }
                    if (featureMatcher.hasFeature("has_custom_resolution", Boolean.TRUE)) {
                        result.addAll(Arrays.asList("--width", substitutor.replace("${resolution_width}"), "--height", substitutor.replace("${resolution_height}")));
                    }
                } else if (type == ArgumentType.JVM) {

                    result.add("-Dfml.ignoreInvalidMinecraftCertificates=true");
                    result.add("-Dfml.ignorePatchDiscrepancies=true");
                    result.add("-Djava.net.useSystemProxies=true");

                    if (OS.WINDOWS.isCurrent()) {
                        result.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
                        if (OS.VERSION.startsWith("10.")) {
                            result.addAll(Arrays.asList("-Dos.name=Windows 10", "-Dos.version=10.0"));
                        }
                    } else if (OS.OSX.isCurrent()) {
                        result.addAll(Arrays.asList(substitutor.replace("-Xdock:icon=${asset=icons/minecraft.icns}"), "-Xdock:name=Minecraft"));
                    }
                    result.add(substitutor.replace("-Djava.library.path=${natives_directory}"));
                    result.add(substitutor.replace("-Dminecraft.launcher.brand=${launcher_name}"));
                    result.add(substitutor.replace("-Dminecraft.launcher.version=${launcher_version}"));
                    result.add(substitutor.replace("-Dminecraft.client.jar=${primary_jar}"));
                    result.addAll(Arrays.asList("-cp", substitutor.replace("${classpath}")));
                }
            }
        }
        return result;
    }

    public Map<String, LoggingConfiguration> getLogging() {
        return logging;
    }

    public void validate() {
        Validate.notNull(id, "id");
        Validate.notNull(type, "type");
        Validate.notNull(libraries, "libraries");
        Validate.notNull(mainClass, "mainClass");
    }

    protected static final Pattern familyPattern = Pattern.compile("([a-z]*[\\d]\\.[\\d]+).*");

    public static String getFamilyOf(String id) {
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
            builder.registerTypeAdapter(Date.class, new DateTypeAdapter(true));
            builder.registerTypeAdapter(Argument.class, new Argument.Serializer());
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
                    version.type = ReleaseType.UNKNOWN.getName();
                }

                if (version.source == null) {
                    version.source = Repository.LOCAL_VERSION_REPO;
                }

                if (version.time == null) {
                    version.time = new Date(0L);
                }

                if (version.libraries == null) {
                    version.libraries = new ArrayList<>();
                }

                return version;
            }
        }

        public JsonElement serialize(CompleteVersion version0, Type type, JsonSerializationContext context) {
            CompleteVersion version = version0.clone();
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

    @Override
    public CompleteVersion clone() {
        CompleteVersion c;
        try {
            c = (CompleteVersion) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        if (this.arguments != null) {
            c.arguments = new java.util.EnumMap<>(ArgumentType.class);
            for (Map.Entry<ArgumentType, List<Argument>> argsList : arguments.entrySet()) {
                c.arguments.put(argsList.getKey(), new ArrayList<>(argsList.getValue()));
            }
        }

        return c;
    }

    public static class JavaVersion {
        private String component;
        private int majorVersion;

        public JavaVersion(String component, int majorVersion) {
            this.component = component;
            this.majorVersion = majorVersion;
        }

        public JavaVersion() {
        }

        public String getComponent() {
            return component;
        }

        public int getMajorVersion() {
            return majorVersion;
        }


        @Override
        public String toString() {
            return "JavaVersion{" +
                    "component='" + component + '\'' +
                    ", majorVersion=" + majorVersion +
                    '}';
        }
    }

    public static class LoggingConfiguration {
        private String argument;
        private LoggingDownloadInfo file;
        private String type;

        public String getArgument() {
            return argument;
        }

        public LoggingDownloadInfo getFile() {
            return file;
        }

        public String getType() {
            return type;
        }

        public boolean isValid() {
            return argument != null && file != null && "log4j2-xml".equals(type);
        }
    }

    public static class LoggingDownloadInfo extends DownloadInfo {
        private String id;

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return "LoggingDownloadInfo{" +
                    "url='" + url + '\'' +
                    ", sha1='" + sha1 + '\'' +
                    ", size=" + size +
                    ", id='" + id + '\'' +
                    '}';
        }
    }
}
