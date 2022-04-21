package ru.turikhay.tlauncher.bootstrap.meta;

import com.github.zafarkhaja.semver.Version;

public interface BootstrapMeta {
    String BETA_BRANCH = "legacy_beta";

    Version getVersion();

    String getShortBrand();
}
