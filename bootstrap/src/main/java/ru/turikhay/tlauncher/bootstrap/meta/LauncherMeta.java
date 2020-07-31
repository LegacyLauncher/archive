package ru.turikhay.tlauncher.bootstrap.meta;

import com.github.zafarkhaja.semver.Version;
import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.tlauncher.bootstrap.json.ToStringBuildable;

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
