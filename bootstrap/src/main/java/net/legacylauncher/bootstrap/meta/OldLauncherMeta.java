package net.legacylauncher.bootstrap.meta;

import com.github.zafarkhaja.semver.Version;
import lombok.Value;
import net.legacylauncher.bootstrap.launcher.Library;

import java.util.List;

@Value
public class OldLauncherMeta implements LauncherMeta {
    String brand;
    String shortBrand;
    String mainClass;
    Version version;
    List<Library> libraries;

    public LocalLauncherMeta toModernMeta() {
        return new LocalLauncherMeta(this);
    }
}

