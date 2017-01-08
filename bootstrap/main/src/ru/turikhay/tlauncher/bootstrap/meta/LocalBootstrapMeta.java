package ru.turikhay.tlauncher.bootstrap.meta;

import ru.turikhay.tlauncher.bootstrap.launcher.LaunchType;
import shaded.org.apache.commons.lang3.builder.ToStringBuilder;

public class LocalBootstrapMeta extends BootstrapMeta {
    private boolean forceUpdate;
    private LaunchType launchType;

    public LocalBootstrapMeta() {
    }

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

    public LaunchType getLaunchType() {
        return launchType;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public void setLaunchType(LaunchType launchType) {
        this.launchType = launchType;
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder()
                .append("forceUpdate", forceUpdate)
                .append("launchType", launchType);
    }
}
