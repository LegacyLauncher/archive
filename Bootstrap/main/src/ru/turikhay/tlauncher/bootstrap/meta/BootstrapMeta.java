package ru.turikhay.tlauncher.bootstrap.meta;

import com.github.zafarkhaja.semver.Version;

public class BootstrapMeta {
    private Version version;
    private String shortBrand;

    public Version getVersion() {
        return version;
    }

    protected void setVersion(Version version) {
        this.version = version;
    }

    public String getShortBrand() {
        return shortBrand;
    }

    protected void setShortBrand(String shortBrand) {
        this.shortBrand = shortBrand;
    }
}
