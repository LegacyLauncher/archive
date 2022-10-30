package ru.turikhay.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * https://www.oracle.com/technetwork/java/javase/versioning-naming-139433.html
 */
public final class JavaVersion implements Comparable<JavaVersion> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaVersion.class);

    public static final JavaVersion UNKNOWN =
            new JavaVersion("unknown", "unknown", 1, 0, 0, 0);

    private static JavaVersion CURRENT;

    public static JavaVersion getCurrent() {
        if (CURRENT == null) {
            String sVersion = System.getProperty("java.version");
            JavaVersion version;
            try {
                version = parse(sVersion);
            } catch (RuntimeException rE) {
                LOGGER.warn("Could not parse java version: {}", sVersion, rE);
                version = UNKNOWN;
            }
            CURRENT = version;
        }
        return CURRENT;
    }

    private final String version, identifier;
    private final int epoch, major, minor, update;
    private final boolean ea;

    private final double d;

    private JavaVersion(String version, String identifier, int epoch, int major, int minor, int update) {
        if (StringUtils.isBlank(version)) {
            throw new IllegalArgumentException("version");
        }
        this.version = version;
        identifier = StringUtils.isBlank(identifier) ? null : identifier;

        if (epoch == 1) {
            this.epoch = epoch;
            this.major = ifNotNegative(major, "major");
            this.minor = ifNotNegative(minor, "minor");

            if (identifier != null && update == 0) {
                update = -1;
                if (!identifier.equals("unknown")) {
                    if (identifier.startsWith("u")) {
                        update = parse(identifier.substring(1), "update in identifier (starting with \"u\")");
                        identifier = null;
                    } else {
                        Integer updateInIdentifier = asInteger(identifier);
                        if (updateInIdentifier != null) {
                            update = updateInIdentifier;
                            identifier = null;
                        }
                    }
                }
            }
            this.update = ifNotSmallerMinusOne(update, "update");
        } else if (epoch == 0 && ifPositive(major, "major") > 0) {
            this.epoch = 1;
            this.major = major;
            this.minor = ifNotNegative(minor, "minor (java 9+)");
            this.update = ifNotSmallerMinusOne(minor, "update (java 9+)");
        } else {
            this.epoch = 1;
            this.major = ifPositive(epoch, "major (java 9+)");
            this.minor = ifNotNegative(major, "minor (java 9+)");
            this.update = ifNotSmallerMinusOne(minor, "update (java 9+)");
        }
        this.identifier = identifier;
        ea = version.contains("-ea");
        d = Double.parseDouble(this.epoch + "." + this.major);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaVersion that = (JavaVersion) o;
        return version.equals(that.version);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = version != null ? version.hashCode() : 0;
        result = 31 * result + (identifier != null ? identifier.hashCode() : 0);
        result = 31 * result + epoch;
        result = 31 * result + major;
        result = 31 * result + minor;
        result = 31 * result + update;
        result = 31 * result + (ea ? 1 : 0);
        temp = Double.doubleToLongBits(d);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public int compareTo(JavaVersion o) {
        if (o == null) {
            throw new NullPointerException("version");
        }

        int epochCompare = Integer.compare(getEpoch(), o.getEpoch());
        if (epochCompare != 0) {
            return epochCompare;
        }

        int majorCompare = Integer.compare(getMajor(), o.getMajor());
        if (majorCompare != 0) {
            return majorCompare;
        }

        int minorCompare = Integer.compare(getMinor(), o.getMinor());
        if (minorCompare != 0) {
            return minorCompare;
        }

        int updateCompare = Integer.compare(getUpdate(), o.getUpdate());
        if (updateCompare != 0) {
            return updateCompare;
        }

        return Boolean.compare(isRelease(), o.isRelease());
    }

    public String getVersion() {
        return version;
    }

    public String getIdentifier() {
        return identifier;
    }

    public double getDouble() {
        return d;
    }

    public int getEpoch() {
        return epoch;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getUpdate() {
        return update;
    }

    public boolean isEarlyAccess() {
        return ea;
    }

    public boolean isRelease() {
        return identifier == null;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("version", getVersion())
                .append("identifier", getIdentifier())
                .append("epoch", getEpoch())
                .append("major", getMajor())
                .append("minor", getMinor())
                .append("update", getUpdate())
                .append("ea", isEarlyAccess())
                .append("release", isRelease())
                .build();
    }

    public static JavaVersion create(int epoch, int major, int minor, int update) {
        return new JavaVersion(epoch + "." + major + "." + minor + (update > 0 ? "_" + update : ""), null, epoch, major, minor, update);
    }

    private static final Pattern pattern = Pattern.compile("(?:([0-9]+)\\.)?([0-9]+)(?:\\.([0-9]+))?(?:\\.[0-9]+)*(?:_([0-9]+)(?:b[0-9]+)?)?(?:-(.+))?");

    public static JavaVersion parse(String version) {
        if (StringUtils.isBlank(version)) {
            throw new IllegalArgumentException("version");
        }
        Matcher matcher = pattern.matcher(version);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("could not parse java version");
        }

        if (matcher.groupCount() != 5) {
            throw new IllegalArgumentException("illegal group count: " + matcher.groupCount());
        }

        return new JavaVersion(version, matcher.group(5),
                parse(matcher.group(1), "epoch", true),
                parse(matcher.group(2), "major"),
                parse(matcher.group(3), "minor", true),
                parse(matcher.group(4), "update", true)
        );
    }

    private static int parse(String str, String name, boolean zeroifNull) {
        if (StringUtils.isEmpty(str)) {
            if (zeroifNull) {
                return 0;
            }
            throw new IllegalArgumentException("could not parse " + name, null);
        }

        try {
            return Integer.parseInt(str);
        } catch (RuntimeException rE) {
            throw new IllegalArgumentException("could not parse " + name, rE);
        }
    }

    private static int parse(String str, String name) {
        return parse(str, name, false);
    }

    private static Integer asInteger(String str) {
        try {
            return Integer.parseInt(str);
        } catch (RuntimeException rE) {
            return null;
        }
    }

    private static int ifPositive(int num, String name) {
        if (num <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return num;
    }

    private static int ifNotNegative(int num, String name) {
        if (num < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return num;
    }

    private static int ifNotSmallerMinusOne(int num, String name) {
        if (num < -1) {
            throw new IllegalArgumentException(name + " must not be less than -1");
        }
        return num;
    }
}
