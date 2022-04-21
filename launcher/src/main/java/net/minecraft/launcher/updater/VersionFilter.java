package net.minecraft.launcher.updater;

import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;

import java.util.*;

public class VersionFilter {
    private final Set<ReleaseType> types = new HashSet<>(ReleaseType.valuesCollection());
    private final Set<ReleaseType.SubType> subTypes = new HashSet<>(ReleaseType.SubType.valuesCollection());

    public Set<ReleaseType> getTypes() {
        return types;
    }

    public Set<ReleaseType.SubType> getSubTypes() {
        return subTypes;
    }

    public VersionFilter onlyForType(ReleaseType... types) {
        this.types.clear();
        include(types);
        return this;
    }

    public VersionFilter onlyForType(ReleaseType.SubType... subTypes) {
        this.subTypes.clear();
        include(subTypes);
        return this;
    }

    public VersionFilter include(ReleaseType... types) {
        if (types != null) {
            Collections.addAll(this.types, types);
        }

        return this;
    }

    public VersionFilter include(ReleaseType.SubType... types) {
        if (types != null) {
            Collections.addAll(subTypes, types);
        }

        return this;
    }

    public VersionFilter exclude(ReleaseType... types) {
        if (types != null) {
            Arrays.asList(types).forEach(this.types::remove);
        }

        return this;
    }

    public VersionFilter exclude(ReleaseType.SubType... types) {
        if (types != null) {
            Arrays.asList(types).forEach(this.subTypes::remove);
        }

        return this;
    }

    public boolean satisfies(Version v) {
        ReleaseType releaseType = v.getReleaseType();
        if (releaseType == null) {
            return true;
        } else if (!types.contains(releaseType)) {
            return false;
        } else {
            List<ReleaseType.SubType> subTypeList = ReleaseType.SubType.get(v);
            for (ReleaseType.SubType subType : subTypeList)
                if (!subTypes.contains(subType))
                    return false;
            return true;
            //return subType == null ? true : subTypes.contains(subType);
        }
    }

    public String toString() {
        return "VersionFilter" + types;
    }
}
