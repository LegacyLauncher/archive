package ru.turikhay.tlauncher.bootstrap.launcher;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.tlauncher.bootstrap.json.ToStringBuildable;
import ru.turikhay.tlauncher.bootstrap.task.DownloadTask;
import ru.turikhay.tlauncher.repository.RepoPrefixV1;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class Library extends ToStringBuildable {
    private static final List<String> LIBRARY_REPO_LIST;

    static {
        LIBRARY_REPO_LIST = Collections.unmodifiableList(
                RepoPrefixV1.prefixesCdnLast()
                        .stream()
                        .map(prefix -> prefix + "/repo/libraries/")
                        .collect(Collectors.toList())
        );
    }

    private String name, checksum;

    public String getName() {
        return name;
    }

    public Path getFile(Path folder) {
        return Objects.requireNonNull(folder, "folder").resolve(getPath());
    }

    public DownloadTask downloadTo(Path dest) {
        return new DownloadTask(name, getUrlList(), dest, checksum);
    }

    public DownloadTask download(Path folder) {
        return downloadTo(getFile(folder));
    }

    private List<URL> getUrlList() {
        ArrayList<URL> urlList = new ArrayList<>();
        String path = getPath();
        for (String prefix : LIBRARY_REPO_LIST) {
            try {
                urlList.add(new URL(prefix + path));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return urlList;
    }

    private String getFilename() {
        final String[] parts = getParts();
        if (parts.length == 4) {
            return String.format(Locale.ROOT, "%s-%s-%s.jar", parts[1], parts[2], parts[3]);
        } else {
            return String.format(Locale.ROOT, "%s-%s.jar", parts[1], parts[2]);
        }
    }

    private String getBaseDir() {
        final String[] parts = getParts();
        return String.format(java.util.Locale.ROOT, "%s/%s/%s", StringUtils.replaceChars(parts[0], '.', '/'), parts[1], parts[2]);
    }

    public String getPath() {
        return String.format(java.util.Locale.ROOT, "%s/%s", getBaseDir(), getFilename());
    }

    @Expose
    private String[] parts;

    private String[] getParts() {
        if (parts == null) {
            parts = StringUtils.split(Objects.requireNonNull(name, "name"), ":", 4);
        }
        return parts;
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder()
                .append("name", name)
                .append("checksum", checksum);
    }
}
