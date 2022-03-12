package ru.turikhay.tlauncher.bootstrap.meta;

import com.github.zafarkhaja.semver.Version;

public interface BootstrapMeta {
    Version getVersion();

    String getShortBrand();
}
