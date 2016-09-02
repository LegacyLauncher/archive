package ru.turikhay.tlauncher.bootstrap.meta;

import ru.turikhay.tlauncher.bootstrap.launcher.LaunchType;

import java.io.File;

public class LocalBootstrapMeta extends BootstrapMeta {
    private boolean forceUpdate, unpackLauncher;
    private LaunchType launchType;
    private File localFile;

    public LocalBootstrapMeta(BootstrapMeta meta) {
        setVersion(meta.getVersion());
        setShortBrand(meta.getShortBrand());
    }

    public void setShortBrand(String shortBrand) {
        super.setShortBrand(shortBrand);
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public boolean isUnpackLauncher() {
        return unpackLauncher;
    }

    public LaunchType getLaunchType() {
        return launchType;
    }

    public File getLocalFile() {
        return localFile;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public void setUnpackLauncher(boolean unpackLauncher) {
        this.unpackLauncher = unpackLauncher;
    }

    public void setLaunchType(LaunchType launchType) {
        this.launchType = launchType;
    }

    public void setLocalFile(File localFile) {
        this.localFile = localFile;
    }
}
