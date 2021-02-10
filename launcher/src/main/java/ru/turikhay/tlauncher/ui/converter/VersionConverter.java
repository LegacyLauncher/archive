package ru.turikhay.tlauncher.ui.converter;

import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.ReleaseType;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableStringConverter;

public class VersionConverter extends LocalizableStringConverter<VersionSyncInfo> {
    private static final VersionSyncInfo LOADING = VersionSyncInfo.createEmpty();
    private static final VersionSyncInfo EMPTY = VersionSyncInfo.createEmpty();
    private final VersionManager vm;

    public VersionConverter(VersionManager vm) {
        super(null);
        if (vm == null) {
            throw new NullPointerException();
        } else {
            this.vm = vm;
        }
    }

    public String toString(VersionSyncInfo from) {
        if (from == null) {
            return null;
        } else if (from.equals(LOADING)) {
            return Localizable.get("versions.loading");
        } else if (from.equals(EMPTY)) {
            return Localizable.get("versions.notfound.tip");
        } else {
            String id = from.getID();
            ReleaseType type = from.getLatestVersion().getReleaseType();
            if (type != null && !type.equals(ReleaseType.UNKNOWN)) {
                String typeF = type.toString().toLowerCase(java.util.Locale.ROOT);
                String formatted = Localizable.get().nget("version." + typeF, id);
                return formatted == null ? id : formatted;
            } else {
                return id;
            }
        }
    }

    public VersionSyncInfo fromString(String from) {
        return vm.getVersionSyncInfo(from);
    }

    public String toValue(VersionSyncInfo from) {
        return null;
    }

    public String toPath(VersionSyncInfo from) {
        return null;
    }

    public Class<VersionSyncInfo> getObjectClass() {
        return VersionSyncInfo.class;
    }
}
