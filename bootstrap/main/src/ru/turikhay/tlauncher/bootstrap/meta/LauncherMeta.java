package ru.turikhay.tlauncher.bootstrap.meta;

import ru.turikhay.tlauncher.bootstrap.json.ToStringBuildable;
import shaded.com.github.zafarkhaja.semver.Version;
import ru.turikhay.tlauncher.bootstrap.launcher.Library;
import shaded.org.apache.commons.lang3.builder.ToStringBuilder;
import shaded.org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

public abstract class LauncherMeta extends ToStringBuildable {
    private String version;
    private String shortBrand;

    public Version getVersion() {
        return Version.valueOf(version);
    }

    public String getShortBrand() {
        return shortBrand;
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder()
                .append("version", version)
                .append("shortBrand", shortBrand);
    }
}
