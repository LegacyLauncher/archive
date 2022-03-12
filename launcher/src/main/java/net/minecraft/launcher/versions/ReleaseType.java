package net.minecraft.launcher.versions;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.repository.Repository;

import javax.annotation.Nullable;
import java.util.*;

public enum ReleaseType {
    RELEASE("release", false, true),
    SNAPSHOT("snapshot", true, true),
    MODIFIED("modified", true, true),
    OLD_BETA("old-beta", true, false),
    OLD_ALPHA("old-alpha", true, false),
    LAUNCHER("launcher", true, false),
    PENDING("pending", true, true),
    UNKNOWN("unknown", false, false);

    private static final Logger LOGGER = LogManager.getLogger();

    private static final Map<String, ReleaseType> lookup;
    private static final List<ReleaseType> defaultTypes;
    private static final List<ReleaseType> definableTypes;
    private final String name;
    private final boolean isDefinable;
    private final boolean isDefault;

    private static final Collection<ReleaseType> VALUES = Arrays.asList(values());

    @Nullable
    public static ReleaseType parse(String name) {
        return VALUES.stream().filter(e -> e.name().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    static {
        Map<String, ReleaseType> types = new HashMap<>(values().length);
        List<ReleaseType> deflTypes = new ArrayList<>();
        List<ReleaseType> defnTypes = new ArrayList<>();
        ReleaseType[] var6;
        int var5 = (var6 = values()).length;

        for (int var4 = 0; var4 < var5; ++var4) {
            ReleaseType type = var6[var4];
            types.put(type.getName(), type);
            if (type.isDefault()) {
                deflTypes.add(type);
            }

            if (type.isDefinable()) {
                defnTypes.add(type);
            }
        }

        lookup = Collections.unmodifiableMap(types);
        defaultTypes = Collections.unmodifiableList(deflTypes);
        definableTypes = Collections.unmodifiableList(defnTypes);
    }

    ReleaseType(String name, boolean isDefinable, boolean isDefault) {
        this.name = name;
        this.isDefinable = isDefinable;
        this.isDefault = isDefault;
    }

    String getName() {
        return name;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public boolean isDefinable() {
        return isDefinable;
    }

    public String toString() {
        return super.toString().toLowerCase(java.util.Locale.ROOT);
    }

    public static ReleaseType getByName(String name) {
        return lookup.get(name);
    }

    public static Collection<ReleaseType> valuesCollection() {
        return lookup.values();
    }

    public static List<ReleaseType> getDefault() {
        return defaultTypes;
    }

    public static List<ReleaseType> getDefinable() {
        return definableTypes;
    }

    public enum SubType {
        OLD_RELEASE("old_release") {
            private final Date marker;

            {
                GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                calendar.set(2014, Calendar.MAY, 14, 0, 0);
                marker = calendar.getTime();
            }

            public boolean isSubType(Version version) {
                if (version.getReleaseType().toString().startsWith("old")) {
                    return false;
                }

                Date date = version.getReleaseTime();
                if (date == null) {
                    LOGGER.warn("release time null: {}", version.getID());
                    return false;
                }

                if (date.getTime() <= 0) {
                    if (StringUtils.containsIgnoreCase(version.getID(), "forge")) {
                        date = version.getUpdatedTime();
                    } else {
                        return false;
                    }
                }

                return date.before(marker);
            }
        },
        REMOTE("remote") {
            public boolean isSubType(Version version) {
                return version.getSource() != Repository.LOCAL_VERSION_REPO;
            }
        };

        private static final Map<String, ReleaseType.SubType> lookup;
        private static final List<ReleaseType.SubType> defaultSubTypes;
        private final String name;
        private final boolean isDefault;

        static {
            Map<String, ReleaseType.SubType> subTypes = new HashMap<>(values().length);
            List<ReleaseType.SubType> defSubTypes = new ArrayList<>();
            ReleaseType.SubType[] var5;
            int var4 = (var5 = values()).length;

            for (int var3 = 0; var3 < var4; ++var3) {
                ReleaseType.SubType subType = var5[var3];
                subTypes.put(subType.getName(), subType);
                if (subType.isDefault()) {
                    defSubTypes.add(subType);
                }
            }

            lookup = Collections.unmodifiableMap(subTypes);
            defaultSubTypes = Collections.unmodifiableList(defSubTypes);
        }

        SubType(String name, boolean isDefault) {
            this.name = name;
            this.isDefault = isDefault;
        }

        SubType(String name) {
            this(name, true);
        }

        public String getName() {
            return name;
        }

        public boolean isDefault() {
            return isDefault;
        }

        public String toString() {
            return super.toString().toLowerCase(java.util.Locale.ROOT);
        }

        public static ReleaseType.SubType getByName(String name) {
            return lookup.get(name);
        }

        public static Collection<ReleaseType.SubType> valuesCollection() {
            return lookup.values();
        }

        public static List<ReleaseType.SubType> getDefault() {
            return defaultSubTypes;
        }

        public static List<ReleaseType.SubType> get(Version version) {
            ArrayList<ReleaseType.SubType> result = new ArrayList<>();
            ReleaseType.SubType[] var4;
            int var3 = (var4 = values()).length;

            for (int var2 = 0; var2 < var3; ++var2) {
                ReleaseType.SubType subType = var4[var2];
                if (subType.isSubType(version)) {
                    result.add(subType);
                }
            }

            return result;
        }

        public abstract boolean isSubType(Version var1);
    }

    public static ReleaseType of(String input) {
        ReleaseType value = ReleaseType.parse(input);
        return value == null ? ReleaseType.UNKNOWN : value;
    }
}
