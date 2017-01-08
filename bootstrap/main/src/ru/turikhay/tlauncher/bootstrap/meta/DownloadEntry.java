package ru.turikhay.tlauncher.bootstrap.meta;

import ru.turikhay.tlauncher.bootstrap.task.DownloadTask;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownloadEntry {
    private String name;
    private final List<URL> url = new ArrayList<URL>();
    private String checksum;

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

    public DownloadTask toDownloadTask(String name, File destFile) {
        return new DownloadTask(name, url, destFile, checksum);
    }
}
