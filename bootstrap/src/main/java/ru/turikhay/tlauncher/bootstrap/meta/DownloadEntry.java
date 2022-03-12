package ru.turikhay.tlauncher.bootstrap.meta;

import ru.turikhay.tlauncher.bootstrap.task.DownloadTask;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DownloadEntry {
    private String name;
    private final List<URL> url = new ArrayList<>();
    private String checksum;

    public DownloadEntry(String name, List<URL> url, String checksum) {
        this.name = name;
        this.url.addAll(url);
        this.checksum = checksum;
    }

    public DownloadEntry() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<URL> getUrl() {
        return url;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public DownloadTask toDownloadTask(String name, Path destFile) {
        return new DownloadTask(name, url, destFile, checksum);
    }
}
