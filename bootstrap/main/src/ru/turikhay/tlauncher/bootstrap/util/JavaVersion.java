package ru.turikhay.tlauncher.bootstrap.util;

import shaded.org.apache.commons.lang3.StringUtils;
import shaded.org.apache.commons.lang3.builder.ToStringBuilder;
import shaded.org.apache.commons.lang3.builder.ToStringStyle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * http://www.oracle.com/technetwork/java/javase/versioning-naming-139433.html
 */
public final class JavaVersion implements Comparable<JavaVersion> {
    private static JavaVersion CURRENT;

    public static JavaVersion getCurrent() {
        if(CURRENT == null) {
            String sVersion = System.getProperty("java.version");
            JavaVersion version;
            try {
                version = parse(sVersion);
            } catch(RuntimeException rE) {
                U.log("Could not parse java version:", sVersion, rE);
                version = JavaVersion.parse("1.6.0");
            }
            CURRENT = version;
        }
        return CURRENT;
    }

    private final String version, identifier;
    private final int epoch, major, minor, update;
    private final boolean beta, ea;

    private final double d;

    private JavaVersion(String version, String identifier, int epoch, int major, int minor, int update) {
        if(StringUtils.isBlank(version)) {
            throw new IllegalArgumentException("version");
        }
        this.version = version;
        this.identifier = StringUtils.isBlank(identifier) ? null : identifier;

        this.epoch = ifPositive(epoch, "epoch");
        this.major = ifPositive(major, "major");
        this.minor = ifNotNegative(minor, "minor");

        if (identifier != null && update == 0) {
            update = -1;
        }
        this.update = ifNotSmallerMinusOne(update, "update");

        beta = Pattern.compile(".+-b[0-9]+.*").matcher(version).matches();
        ea = version.contains("-ea");

        d = Double.parseDouble(epoch + "." + major);
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

    public boolean isBeta() {
        return beta;
    }

    public boolean isEarlyAccess() {
        return ea;
    }

    public boolean isRelease() {
        return identifier == null;
    }

    @Override
    public int compareTo(JavaVersion o) {
        U.requireNotNull(o, "version");

        int epochCompare = compare(getEpoch(), o.getEpoch());
        if(epochCompare != 0) {
            return epochCompare;
        }

        int majorCompare = compare(getMajor(), o.getMajor());
        if(majorCompare != 0) {
            return majorCompare;
        }

        int minorCompare = compare(getMinor(), o.getMinor());
        if(minorCompare != 0) {
            return minorCompare;
        }

        int updateCompare = compare(getUpdate(), o.getUpdate());
        if(updateCompare != 0) {
            return updateCompare;
        }

        int currentRelease = boolToInt(isRelease()), compareRelease = boolToInt(o.isRelease());
        return currentRelease - compareRelease; // 00,11 = 0; 01 = -1; 10 = 1
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
                .append("beta", isBeta())
                .append("ea", isEarlyAccess())
                .append("release", isRelease())
                .build();
    }

    public static JavaVersion create(int epoch, int major, int minor, int update) {
        return new JavaVersion(epoch + "." + major + "." + minor + (update > 0 ? "_" + update : ""), null, epoch, major, minor, update);
    }

    private static final Pattern pattern = Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:_([0-9]+))?(?:-(.+))?");

    public static JavaVersion parse(String version) {
        if(StringUtils.isBlank(version)) {
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
                parse(matcher.group(1), "epoch"),
                parse(matcher.group(2), "major"),
                parse(matcher.group(3), "minor"),
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

    private static int compare(int i0, int i1) {
        return (i0 < i1? -1 : (i0 == i1? 0 : 1));
    }

    private static int boolToInt(boolean b) {
        return b? 1 : 0;
    }
}
