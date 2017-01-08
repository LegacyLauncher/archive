package net.minecraft.launcher.updater;

import ru.turikhay.util.U;

public class AssetIndexInfo extends DownloadInfo {
    protected long totalSize;
    protected String id;
    protected boolean known = true;

    public AssetIndexInfo() {
    }

    public AssetIndexInfo(String id) {
        this.id = id;
        url = U.makeURL("https://s3.amazonaws.com/Minecraft.Download/indexes/" + id + ".json");
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
}
