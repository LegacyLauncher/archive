package ru.turikhay.tlauncher.bootstrap.meta;

import com.github.zafarkhaja.semver.Version;
import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.tlauncher.bootstrap.json.ToStringBuildable;

public abstract class LauncherMeta extends ToStringBuildable {
    private Version version;
    private String shortBrand;

    public LauncherMeta(Version version, String shortBrand) {
        this.version = version;
        this.shortBrand = shortBrand;
    }

    public LauncherMeta() {
    }

    public Version getVersion() {
        return version;
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
