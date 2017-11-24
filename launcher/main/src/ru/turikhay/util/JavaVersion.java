package ru.turikhay.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * http://www.oracle.com/technetwork/java/javase/versioning-naming-139433.html
 */
public final class JavaVersion {
    private final String version, identifier;
    private final int epoch, major, minor, update;
    private final boolean ea;

    private final double d;

    private JavaVersion(String version, String identifier, int epoch, int major, int minor, int update) {
        this.version = StringUtil.requireNotBlank(version, "version");
        this.identifier = StringUtils.isBlank(identifier) ? null : identifier;

        if(epoch == 1) {
            this.epoch = epoch;
            this.major = ifPositive(major, "major");
            this.minor = ifNotNegative(minor, "minor");

            if (identifier != null && update == 0) {
                update = -1;
            }
            this.update = ifNotSmallerMinusOne(update, "update");
        } else if(epoch == 0 && ifPositive(major, "major") > 0) {
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
        ea = version.contains("-ea");
        d = Double.parseDouble(this.epoch + "." + this.major);
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

    private static final Pattern pattern = Pattern.compile("(?:([0-9]+)\\.)?([0-9]+)(?:\\.([0-9]+))?(?:_([0-9]+))?(?:-(.+))?");

    public static JavaVersion parse(String version) {
        Matcher matcher = pattern.matcher(StringUtil.requireNotBlank(version, "version"));

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
        RuntimeException nested = null;

        parsing:
        {
            if (StringUtils.isEmpty(str)) {
                if (zeroifNull) {
                    return 0;
                }
                break parsing;
            }

            try {
                return Integer.parseInt(str);
            } catch (RuntimeException rE) {
                nested = rE;
            }
        }

        throw new IllegalArgumentException("could not parse " + name, nested);
    }

    private static int parse(String str, String name) {
        return parse(str, name, false);
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
