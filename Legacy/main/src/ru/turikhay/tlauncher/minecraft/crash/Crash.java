package ru.turikhay.tlauncher.minecraft.crash;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.File;

public final class Crash {
    private final CrashManager manager;

    private CrashEntry entry;
    private File crashFile, nativeCrashFile;

    Crash(CrashManager manager) {
        this.manager = manager;
    }

    public CrashManager getManager() {
        return manager;
    }

    public CrashEntry getEntry() {
        return entry;
    }

    void setEntry(CrashEntry entry) {
        this.entry = entry;
    }

    public File getCrashFile() {
        return crashFile;
    }

    void setCrashFile(String path) {
        crashFile = new File(path);
    }

    public File getNativeCrashFile() {
        return nativeCrashFile;
    }

    void setNativeCrashFile(String path) {
        nativeCrashFile = new File(path);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("entry", entry)
                .append("crashFile", crashFile)
                .append("nativeCrashFile", nativeCrashFile)
                .build();
    }
}
