package ru.turikhay.tlauncher.bootstrap.meta;

import com.github.zafarkhaja.semver.Version;
import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.tlauncher.bootstrap.json.ToStringBuildable;

public class RemoteBootstrapMeta extends ToStringBuildable implements BootstrapMeta {
    private final DownloadEntry download;
    private final Version version;
    private final String shortBrand;

    public RemoteBootstrapMeta(Version version, String shortBrand, DownloadEntry download) {
        this.version = version;
        this.shortBrand = shortBrand;
        this.download = download;
    }

    public Version getVersion() {
        return version;
    }

    public String getShortBrand() {
        return shortBrand;
    }

    public DownloadEntry getDownload() {
        return download;
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder()
                .append("version", version)
                .append("shortBrand", shortBrand)
                .append("download", download);
    }
}
