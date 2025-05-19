package net.legacylauncher.bootstrap.launcher;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import net.legacylauncher.bootstrap.util.U;
import net.legacylauncher.repository.HostsV1;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import net.legacylauncher.bootstrap.json.ToStringBuildable;
import net.legacylauncher.bootstrap.task.DownloadTask;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class Library extends ToStringBuildable {
    @Getter
    private String name, checksum;

    @Getter
    private int javaVersion;

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
        String path = getPath();
        return HostsV1.REPO.stream().map(host -> String.format(Locale.ROOT,
                "https://%s/libraries/%s",
                host, path
        )).map(U::toUrl).collect(Collectors.toList());
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
