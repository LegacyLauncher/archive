package ru.turikhay.tlauncher.bootstrap.meta;

import com.github.zafarkhaja.semver.Version;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class RemoteBootstrapMeta extends BootstrapMeta {
    private DownloadEntry download;

    public RemoteBootstrapMeta(Version version, String shortBrand, DownloadEntry download) {
        super(version, shortBrand);
        this.download = download;
    }

    public DownloadEntry getDownload() {
        return download;
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder()
                .append("download", download);
    }
}
