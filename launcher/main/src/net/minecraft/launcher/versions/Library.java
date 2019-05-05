package net.minecraft.launcher.versions;

import net.minecraft.launcher.updater.DownloadInfo;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.tukaani.xz.XZInputStream;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.RetryDownloadException;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

public class Library {
    protected static final String FORGE_LIB_SUFFIX = ".pack.xz";
    protected static final StrSubstitutor SUBSTITUTOR;
    protected String name;
    protected List<Rule> rules;
    protected Map<OS, String> natives;
    protected ExtractRules extract;
    protected String url;
    protected String exact_url;
    protected String packed;
    protected String checksum;
    protected List<String> deleteEntries;
    protected LibraryDownloadInfo downloads;
    protected Boolean mod;

    static {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("platform", OS.CURRENT.getName());
        map.put("arch", OS.Arch.CURRENT.getBit());
        SUBSTITUTOR = new StrSubstitutor(map);
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
                .append(mod, library.mod)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(rules)
                .append(natives)
                .append(extract)
                .append(mod)
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

    public boolean isMod() {
        return mod != null && mod;
    }

    String getArtifactBaseDir() {
        if (name == null) {
            throw new IllegalStateException("Cannot get artifact dir of empty/blank artifact");
        } else {
            String[] parts = name.split(":", 3);
            return String.format("%s/%s/%s", parts[0].replaceAll("\\.", "/"), parts[1], parts[2]);
        }
    }

    public String getArtifactPath() {
        return getArtifactPath(null);
    }

    public String getArtifactPath(String classifier) {
        if (name == null) {
            throw new IllegalStateException("Cannot get artifact path of empty/blank artifact");
        } else {
            return String.format("%s/%s", getArtifactBaseDir(), getArtifactFilename(classifier));
        }
    }

    String getArtifactFilename(String classifier) {
        if (name == null) {
            throw new IllegalStateException("Cannot get artifact filename of empty/blank artifact");
        } else {
            String[] parts = name.split(":", 3);
            String result;
            if (classifier == null) {
                result = String.format("%s-%s.jar", parts[1], parts[2]);
            } else {
                result = String.format("%s-%s%s.jar", parts[1], parts[2], "-" + classifier);
            }

            return SUBSTITUTOR.replace(result);
        }
    }

    public String toString() {
        return "Library{name=\'" + name + '\'' + ", rules=" + rules + ", natives=" + natives + ", extract=" + extract + ", packed=\'" + packed + "\', mod="+ mod +"}";
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
        boolean forgePacked = "forge".equals(packed);

        if (exact_url == null) {
            path = getArtifactPath(classifier) + (forgePacked ? FORGE_LIB_SUFFIX : "");
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

        if (forgePacked) {
            File tempFile = new File(file.getAbsolutePath() + FORGE_LIB_SUFFIX);
            return new Library.ForgeLibDownloadable(path, tempFile, file);
        }

        return repo == null ? new Library.LibraryDownloadable(path, file) : new Library.LibraryDownloadable(repo, path, file);
    }

    private static synchronized void unpackLibrary(File library, File output, boolean retryOnOutOfMemory) throws IOException {
        forgeLibLog("Synchronized unpacking:", library);
        output.delete();
        XZInputStream in = null;
        JarOutputStream jos = null;

        try {
            FileInputStream in1 = new FileInputStream(library);
            in = new XZInputStream(in1);
            forgeLibLog("Decompressing...");
            byte[] e = readFully(in);
            forgeLibLog("Decompressed successfully");
            String end = new String(e, e.length - 4, 4);
            if (!end.equals("SIGN")) {
                throw new RetryDownloadException("signature missing");
            }

            forgeLibLog("Signature matches!");
            int x = e.length;
            int len = e[x - 8] & 255 | (e[x - 7] & 255) << 8 | (e[x - 6] & 255) << 16 | (e[x - 5] & 255) << 24;
            forgeLibLog("Now getting checksums...");
            byte[] checksums = Arrays.copyOfRange(e, e.length - len - 8, e.length - 8);
            FileUtil.createFile(output);
            FileOutputStream jarBytes = new FileOutputStream(output);
            jos = new JarOutputStream(jarBytes);
            forgeLibLog("Now unpacking...");
            Pack200.newUnpacker().unpack(new ByteArrayInputStream(e), jos);
            forgeLibLog("Unpacked successfully");
            forgeLibLog("Now trying to write checksums...");
            jos.putNextEntry(new JarEntry("checksums.sha1"));
            jos.write(checksums);
            jos.closeEntry();
            forgeLibLog("Now finishing...");
        } catch (OutOfMemoryError var15) {
            forgeLibLog("Out of memory, oops", var15);
            U.gc();
            if (retryOnOutOfMemory) {
                forgeLibLog("Retrying...");
                close(in, jos);
                FileUtil.deleteFile(library);
                unpackLibrary(library, output, false);
                return;
            }

            throw var15;
        } catch (IOException var16) {
            output.delete();
            throw var16;
        } finally {
            close(in, jos);
            FileUtil.deleteFile(library);
        }

        forgeLibLog("Done:", output);
    }

    private static synchronized void unpackLibrary(File library, File output) throws IOException {
        unpackLibrary(library, output, true);
    }

    private static void close(Closeable... closeables) {
        Closeable[] var4 = closeables;
        int var3 = closeables.length;

        for (int var2 = 0; var2 < var3; ++var2) {
            Closeable c = var4[var2];

            try {
                c.close();
            } catch (Exception var6) {
            }
        }

    }

    private static byte[] readFully(InputStream stream) throws IOException {
        byte[] data = new byte[4096];
        ByteArrayOutputStream entryBuffer = new ByteArrayOutputStream();

        int len;
        do {
            len = stream.read(data);
            if (len > 0) {
                entryBuffer.write(data, 0, len);
            }
        } while (len != -1);

        return entryBuffer.toByteArray();
    }

    private static void forgeLibLog(Object... o) {
        U.log("[ForgeLibDownloadable]", o);
    }

    public class ForgeLibDownloadable extends Library.LibraryDownloadable {
        private final File unpacked;

        public ForgeLibDownloadable(String url, File packedLib, File unpackedLib) {
            super(url, packedLib);
            unpacked = unpackedLib;
        }

        protected void onComplete() throws RetryDownloadException {
            super.onComplete();

            try {
                Library.unpackLibrary(getDestination(), unpacked);
            } catch (Throwable var2) {
                throw new RetryDownloadException("cannot unpack forge library", var2);
            }
        }
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
            super(Repository.PROXIFIED_REPO, info.getUrl(), file);
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
