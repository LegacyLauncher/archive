package net.minecraft.launcher.updater;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class AssetIndexInfo extends DownloadInfo {
    protected long totalSize;
    protected String id;
    protected boolean known = true;

    public AssetIndexInfo() {
    }

    public AssetIndexInfo(String id) {
        this.id = id;
        url = "https://s3.amazonaws.com/Minecraft.Download/indexes/" + id + ".json";
        known = false;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public String getId() {
        return id;
    }

    public boolean sizeAndHashKnown() {
        return known;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("url", url)
                .append("sha1", sha1)
                .append("size", size)
                .append("totalSize", totalSize)
                .append("known", known)
                .build();
    }
}
