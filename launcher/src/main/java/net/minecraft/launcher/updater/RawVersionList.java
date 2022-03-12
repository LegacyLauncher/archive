package net.minecraft.launcher.updater;

import net.minecraft.launcher.versions.PartialVersion;
import net.minecraft.launcher.versions.ReleaseType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class RawVersionList {
    final List<PartialVersion> versions = new ArrayList<>();
    final Map<ReleaseType, String> latest = new EnumMap<>(ReleaseType.class);

    public List<PartialVersion> getVersions() {
        return versions;
    }

    public Map<ReleaseType, String> getLatestVersions() {
        return latest;
    }
}
