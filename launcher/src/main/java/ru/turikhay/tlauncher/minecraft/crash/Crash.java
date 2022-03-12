package ru.turikhay.tlauncher.minecraft.crash;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class Crash {
    private final CrashManager manager;

    private CrashEntry entry;
    private String description, javaDescription, stackTrace;
    private File crashFile, nativeCrashFile;
    private final Map<String, String> extraInfo = new HashMap<>();

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

    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    public String getJavaDescription() {
        return javaDescription;
    }

    void setJavaDescription(String javaDescription) {
        this.javaDescription = javaDescription;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
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

    public Map<String, String> getExtraInfo() {
        return new HashMap<>(extraInfo);
    }

    void addExtra(String key, String value) {
        extraInfo.put(key, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("entry", entry)
                .append("description", description)
                .append("javaDesc", javaDescription)
                .append("crashFile", crashFile)
                .append("nativeCrashFile", nativeCrashFile)
                .append("extraInfo", extraInfo)
                .build();
    }
}
