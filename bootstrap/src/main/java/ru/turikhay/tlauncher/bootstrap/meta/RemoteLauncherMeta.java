package ru.turikhay.tlauncher.bootstrap.meta;

import com.github.zafarkhaja.semver.Version;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RemoteLauncherMeta extends LauncherMeta {
    private final String checksum;
    private final List<URL> url;
    private Map<String, String> description;

    public RemoteLauncherMeta(Version version, String shortBrand, String checksum,
                              List<URL> url, Map<String, String> description) {
        super(version, shortBrand);
        this.checksum = checksum;
        this.url = url;
        this.description = description;
    }

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
        return url == null ? null : Collections.unmodifiableList(url);
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder()
                .append("checksum", checksum)
                .append("url", url);
    }
}
