package ru.turikhay.tlauncher.bootstrap.meta;

import ru.turikhay.tlauncher.bootstrap.json.ToStringBuildable;
import com.github.zafarkhaja.semver.Version;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class BootstrapMeta extends ToStringBuildable {
    private Version version;
    private String shortBrand;

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String getShortBrand() {
        return shortBrand;
    }

    public void setShortBrand(String shortBrand) {
        this.shortBrand = shortBrand;
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder()
                .append("version", version)
                .append("shortBrand", shortBrand);
    }
}
