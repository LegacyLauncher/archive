package ru.turikhay.tlauncher.bootstrap.meta;

public class LocalBootstrapMeta extends BootstrapMeta {

    public LocalBootstrapMeta() {
    }

    public LocalBootstrapMeta(BootstrapMeta meta) {
        setVersion(meta.getVersion());
        setShortBrand(meta.getShortBrand());
    }
}
