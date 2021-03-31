package net.minecraft.launcher.versions;

import net.minecraft.launcher.updater.DownloadInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.text.StrSubstitutor;
import pw.modder.tl.modloader.Modloader;
import pw.modder.tl.modloader.extractor.ForgeExtractor;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.RetryDownloadException;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Library {
    protected static final StrSubstitutor SUBSTITUTOR;
    protected String name;
    protected List<Rule> rules;
    protected Map<OS, String> natives;
    protected ExtractRules extract;
    protected String url;
    protected String exact_url;
    protected String checksum;
    protected List<String> deleteEntries;
    protected LibraryDownloadInfo downloads;
    protected Boolean mod;
    protected Boolean downloadOnly;
    protected String type;

    static {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("platform", OS.CURRENT.getName());
        map.put("arch", OS.Arch.CURRENT.getBit());
        SUBSTITUTOR = new StrSubstitutor(map);
    }

    public Library() {}

    public Library(String name, String url, String checksum) {
        this.name = name;
        this.url = url;
        this.checksum = checksum;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Library library = (Library) o;

        return new EqualsBuilder()
                .append(name, library.name)
                .append(rules, library.rules)
                .append(natives, library.natives)
                .append(extract, library.extract)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(rules)
                .append(natives)
                .append(extract)
                .toHashCode();
    }

    public String getName() {
        return name;
    }

    public String getPlainName() {
        String[] split = name.split(":", 3);
        return split[0] + "." + split[1];
    }

    public List<Rule> getRules() {
        return rules == null ? null : Collections.unmodifiableList(rules);
    }

    public boolean appliesToCurrentEnvironment(Rule.FeatureMatcher featureMatcher) {
        if (this.rules == null) return true;
        Rule.Action lastAction = Rule.Action.DISALLOW;

        for (Rule compatibilityRule : this.rules) {
            Rule.Action action = compatibilityRule.getAppliedAction(featureMatcher);
            if (action != null) {
                lastAction = action;
            }
        }
        return lastAction == Rule.Action.ALLOW;
    }


    public Map<OS, String> getNatives() {
        return natives;
    }

    public ExtractRules getExtractRules() {
        return extract;
    }

    public String getChecksum() {
        return checksum;
    }

    public List<String> getDeleteEntriesList() {
        return deleteEntries;
    }

    public LibraryType getLibraryType() {
        if (type == null) {
            if (mod != null && mod) return LibraryType.MODIFICATION;
            if (downloadOnly != null && downloadOnly) return LibraryType.DOWNLOAD_ONLY;
            return LibraryType.LIBRARY;
        }

        return LibraryType.getByName(type);
    }

    String getArtifactBaseDir() {
        if (name == null) {
            throw new IllegalStateException("Cannot get artifact dir of empty/blank artifact");
        } else {
            String[] parts = name.split(":", 4);

            return String.format(java.util.Locale.ROOT, "%s/%s/%s", parts[0].replaceAll("\\.", "/"), parts[1], parts[2]);
        }
    }

    public String getArtifactPath() {
        return getArtifactPath(null);
    }

    public String getArtifactPath(String classifier) {
        if (name == null) {
            throw new IllegalStateException("Cannot get artifact path of empty/blank artifact");
        } else {
            return String.format(java.util.Locale.ROOT, "%s/%s", getArtifactBaseDir(), getArtifactFilename(classifier));
        }
    }

    String getArtifactFilename(String classifier) {
        if (name == null) {
            throw new IllegalStateException("Cannot get artifact filename of empty/blank artifact");
        } else {
            String[] parts = name.split(":", 4);
            String result;
            if (classifier == null) {
                if (parts.length == 4) {
                    result = String.format(java.util.Locale.ROOT, "%s-%s-%s.jar", parts[1], parts[2], parts[3]);
                } else {
                    result = String.format(java.util.Locale.ROOT, "%s-%s.jar", parts[1], parts[2]);
                }
            } else {
                result = String.format(java.util.Locale.ROOT, "%s-%s-%s.jar", parts[1], parts[2], classifier);
            }

            return SUBSTITUTOR.replace(result);
        }
    }

    public boolean hasEmptyUrl() {
        return downloads != null && downloads.getArtifact() != null && StringUtils.isEmpty(downloads.getArtifact().getUrl());
    }

    public String toString() {
        return "Library{name=\"" + name + "\", rules=" + rules + ", natives=" + natives + ", extract=" + extract + ", mod="+ mod +", downloadOnly=\"+" + downloadOnly + "\"}";
    }

    public Downloadable getDownloadable(Repository versionSource, Rule.FeatureMatcher featureMatcher, File file, OS os) {
        String classifier = natives != null && appliesToCurrentEnvironment(featureMatcher) ? natives.get(os) : null;

        if (downloads != null) {
            DownloadInfo info = this.downloads.getDownloadInfo(SUBSTITUTOR.replace(classifier));
            if (info != null) {
                return new LibraryDownloadable(info, file);
            }
        }

        Repository repo;
        String path;

        if (exact_url == null) {
            path = getArtifactPath(classifier);
            if (url == null) {
                repo = Repository.LIBRARY_REPO;
            } else if (url.startsWith("/")) {
                repo = versionSource == null? Repository.EXTRA_VERSION_REPO : versionSource;
                path = url.substring(1) + path;
            } else {
                repo = Repository.PROXIFIED_REPO;
                path = url + path;
            }
        } else {
            repo = Repository.PROXIFIED_REPO;
            path = exact_url;
        }

        return repo == null ? new Library.LibraryDownloadable(path, file) : new Library.LibraryDownloadable(repo, path, file);
    }

    public class LibraryDownloadable extends Downloadable {
        private final String checksum;

        private LibraryDownloadable(Repository repo, String path, File file) {
            super(repo, path, file);
            this.checksum = Library.this.getChecksum();
        }

        private LibraryDownloadable(String path, File file) {
            super(Repository.PROXIFIED_REPO, path, file);
            this.checksum = Library.this.getChecksum();
        }

        private LibraryDownloadable(DownloadInfo info, File file) {
            super(info.getUrl().startsWith("/")? Repository.EXTRA_VERSION_REPO : Repository.PROXIFIED_REPO, info.getUrl(), file);
            this.checksum = info.getSha1();
        }

        public Library getDownloadableLibrary() {
            return Library.this;
        }

        public Library getLibrary() {
            return Library.this;
        }

        @Override
        protected void onComplete() throws RetryDownloadException {
            if (checksum != null) {
                String fileHash = FileUtil.getChecksum(getDestination(), "SHA-1");
                if (fileHash != null && !fileHash.equals(checksum)) {
                    throw new RetryDownloadException("illegal library hash. got: " + fileHash + "; expected: " + checksum);
                }
            }
        }
    }
}
