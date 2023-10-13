package net.legacylauncher.bootstrap.meta;

import net.legacylauncher.bootstrap.launcher.Library;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class LocalLauncherMeta extends LauncherMeta {
    private String brand;
    private String bridgedEntryPoint;
    private String entryPoint;
    private List<Library> libraries;

    public LocalLauncherMeta() {
    }

    public LocalLauncherMeta(OldLauncherMeta old) {
        super(old.getVersion(), old.getShortBrand());
        this.brand = old.getBrand();
        this.bridgedEntryPoint = old.getMainClass();
        this.libraries = old.getLibraries();
    }

    public String getBrand() {
        return brand;
    }

    public String getBridgedEntryPoint() {
        return bridgedEntryPoint;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public List<Library> getLibraries() {
        return libraries;
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder()
                .append("brand", brand)
                .append("bridgedEntryPoint", bridgedEntryPoint)
                .append("entryPoint", entryPoint)
                .append("libraries", libraries);
    }
}
