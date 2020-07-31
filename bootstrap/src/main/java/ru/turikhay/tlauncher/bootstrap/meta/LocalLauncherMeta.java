package ru.turikhay.tlauncher.bootstrap.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.tlauncher.bootstrap.launcher.Library;

import java.util.List;

public class LocalLauncherMeta extends LauncherMeta {
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
}
