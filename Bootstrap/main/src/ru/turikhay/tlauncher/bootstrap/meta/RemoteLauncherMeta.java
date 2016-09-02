package ru.turikhay.tlauncher.bootstrap.meta;

import java.net.URL;
import java.util.Collections;
import java.util.List;

public class RemoteLauncherMeta extends LauncherMeta {
    private String checksum;
    private List<URL> downloads;

    public String getChecksum() {
        return checksum;
    }

    public List<URL> getDownloads() {
        return downloads == null ? null : Collections.unmodifiableList(downloads);
    }
}
