package net.legacylauncher.bootstrap.meta;

import net.legacylauncher.bootstrap.launcher.InternalLauncherMeta;
import net.legacylauncher.bootstrap.launcher.Library;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class OldLauncherMeta extends LauncherMeta {
    private String brand;
    private String mainClass;
    private List<Library> libraries;

    public String getBrand() {
        return brand;
    }


    public String getMainClass() {
        return mainClass;
    }

    public List<Library> getLibraries() {
        return libraries;
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder()
                .append("brand", brand)
                .append("mainClass", mainClass)
                .append("libraries", libraries);
    }

    public LocalLauncherMeta toModernMeta() {
        return new LocalLauncherMeta(this);
    }

    public InternalLauncherMeta toInternalMeta() {
        return new InternalLauncherMeta(this);
    }
}

