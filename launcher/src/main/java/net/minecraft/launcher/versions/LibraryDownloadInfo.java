package net.minecraft.launcher.versions;

import net.minecraft.launcher.updater.DownloadInfo;

import java.util.Map;

public class LibraryDownloadInfo {
    private DownloadInfo artifact;
    private Map<String, DownloadInfo> classifiers;

    public DownloadInfo getArtifact() {
        return artifact;
    }

    public DownloadInfo getDownloadInfo(String classifier) {
        if (classifier == null) {
            return this.artifact;
        }
        return this.classifiers.get(classifier);
    }
}