package net.minecraft.launcher.versions;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.OS;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class LibraryReplace extends Library {
    private Pattern replaces;
    private String args;
    private String mainClass;
    private final List<Library> requires = new ArrayList<>();
    private final List<String> supports = new ArrayList<>();

    public LibraryReplace() {
        url = "/libraries/";
    }

    public Pattern getPattern() {
        return replaces;
    }

    public boolean replaces(Library lib) {
        return replaces != null &&
                replaces.matcher(Objects.requireNonNull(lib, "lib").getName()).matches();
    }

    public String getArgs() {
        return args;
    }

    public String getMainClass() {
        return mainClass;
    }

    public List<Library> getRequirementList() {
        return requires;
    }

    public List<String> getSupportedList() {
        return supports;
    }

    public boolean supports(String version) {
        return supports.contains(version);
    }

    public Downloadable getDownloadable(Repository versionSource, Rule.FeatureMatcher featureMatcher, File file, OS os) {
        return super.getDownloadable(Repository.EXTRA_VERSION_REPO, featureMatcher, file, os);
    }

    public ToStringBuilder toStringBuilder() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", name)
                .append("replaces", replaces)
                .append("args", args)
                .append("requires", requires)
                .append("supports", supports);
    }

    public final String toString() {
        return toStringBuilder().toString();
    }
}
