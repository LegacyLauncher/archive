package ru.turikhay.tlauncher.ui.converter;

import net.minecraft.launcher.versions.ReleaseType;
import ru.turikhay.tlauncher.ui.loc.LocalizableStringConverter;

public class ReleaseTypeConverter extends LocalizableStringConverter<ReleaseType> {
    public ReleaseTypeConverter() {
        super("version.description");
    }

    public ReleaseType fromString(String from) {
        if (from == null) {
            return ReleaseType.UNKNOWN;
        } else {
            ReleaseType[] var5;
            int var4 = (var5 = ReleaseType.values()).length;

            for (int var3 = 0; var3 < var4; ++var3) {
                ReleaseType type = var5[var3];
                if (type.toString().equals(from)) {
                    return type;
                }
            }

            return null;
        }
    }

    public String toValue(ReleaseType from) {
        return from == null ? ReleaseType.UNKNOWN.toString() : from.toString();
    }

    protected String toPath(ReleaseType from) {
        return from == null ? ReleaseType.UNKNOWN.toString() : toValue(from);
    }

    public Class<ReleaseType> getObjectClass() {
        return ReleaseType.class;
    }
}
