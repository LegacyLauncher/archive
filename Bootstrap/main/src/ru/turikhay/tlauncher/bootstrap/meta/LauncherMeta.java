package ru.turikhay.tlauncher.bootstrap.meta;

import com.github.zafarkhaja.semver.Version;

public class LauncherMeta {
    private String version;
    private String shortBrand;

    public Version getVersion() {
        return Version.valueOf(version);
    }

    public String getShortBrand() {
        return shortBrand;
    }
}
