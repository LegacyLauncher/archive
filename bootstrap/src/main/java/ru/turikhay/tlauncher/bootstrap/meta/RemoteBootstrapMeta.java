package ru.turikhay.tlauncher.bootstrap.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;

public class RemoteBootstrapMeta extends BootstrapMeta {
    private static final String DEFAULT_DESC_LOCALE = "en_US";

    private Map<PackageType, DownloadEntry> downloads = new HashMap<PackageType, DownloadEntry>();

    public DownloadEntry getDownload() {
        return getDownload(PackageType.CURRENT);
    }

    public DownloadEntry getDownload(PackageType packageType) {
        return downloads.get(packageType);
    }

    public void setDownload(PackageType packageType, DownloadEntry entry) {
        this.downloads.put(packageType, entry);
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder()
                .append("downloads", downloads);
    }
}
