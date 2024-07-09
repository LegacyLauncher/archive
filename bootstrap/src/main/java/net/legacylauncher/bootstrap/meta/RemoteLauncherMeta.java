package net.legacylauncher.bootstrap.meta;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.annotations.SerializedName;
import lombok.Value;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Value
public class RemoteLauncherMeta implements LauncherMeta {
    Version version;
    String shortBrand;
    String checksum;
    @SerializedName("url")
    List<URL> urls;
    Map<String, String> description;

    public RemoteLauncherMeta(Version version, String shortBrand, String checksum,
                              List<URL> urls, Map<String, String> description) {
        this.version = version;
        this.shortBrand = shortBrand;
        this.checksum = checksum;
        this.urls = urls;
        this.description = description;
    }

    public List<URL> getDownloads() {
        return urls == null ? null : Collections.unmodifiableList(urls);
    }
}
