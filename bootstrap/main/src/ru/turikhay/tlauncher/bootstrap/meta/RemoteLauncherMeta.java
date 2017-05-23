package ru.turikhay.tlauncher.bootstrap.meta;

import ru.turikhay.tlauncher.bootstrap.ui.UserInterface;
import shaded.org.apache.commons.lang3.builder.ToStringBuilder;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RemoteLauncherMeta extends LauncherMeta {
    private String checksum;
    private List<URL> downloads;
    private Map<String, String> description;

    public Map<String, String> getDescription() {
        return description;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public String getChecksum() {
        return checksum;
    }

    public List<URL> getDownloads() {
        return downloads == null ? null : Collections.unmodifiableList(downloads);
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder()
                .append("checksum", checksum)
                .append("downloads", downloads);
    }
}
