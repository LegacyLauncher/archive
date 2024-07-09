package net.legacylauncher.bootstrap.meta;

import com.github.zafarkhaja.semver.Version;

public interface LauncherMeta {
    Version getVersion();

    String getShortBrand();
}
