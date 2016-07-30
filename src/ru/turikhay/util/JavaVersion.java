package ru.turikhay.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public final class JavaVersion {
   private final String version;
   private final String identifier;
   private final int epoch;
   private final int major;
   private final int minor;
   private final int update;
   private final boolean beta;
   private final boolean ea;
   private final double d;
   private static final Pattern pattern = Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:_([0-9]+))?(?:-(.+))?");

   private JavaVersion(String version, String identifier, int epoch, int major, int minor, int update) {
      this.version = StringUtil.requireNotBlank(version, "version");
      this.identifier = StringUtils.isBlank(identifier) ? null : identifier;
      this.epoch = ifPositive(epoch, "epoch");
      this.major = ifPositive(major, "major");
      this.minor = ifNotNegative(minor, "minor");
      if (identifier != null && update == 0) {
         update = -1;
      }

      this.update = ifNotSmallerMinusOne(update, "update");
      this.beta = Pattern.compile(".+-b[0-9]+.*").matcher(version).matches();
      this.ea = version.contains("-ea");
      this.d = Double.parseDouble(epoch + "." + major);
   }

   public String getVersion() {
      return this.version;
   }

   public String getIdentifier() {
      return this.identifier;
   }

   public double getDouble() {
      return this.d;
   }

   public int getEpoch() {
      return this.epoch;
   }

   public int getMajor() {
      return this.major;
   }

   public int getMinor() {
      return this.minor;
   }

   public int getUpdate() {
      return this.update;
   }

   public boolean isBeta() {
      return this.beta;
   }

   public boolean isEarlyAccess() {
      return this.ea;
   }

   public boolean isRelease() {
      return this.identifier == null;
   }

   public String toString() {
      return (new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)).append("version", this.getVersion()).append("identifier", this.getIdentifier()).append("epoch", this.getEpoch()).append("major", this.getMajor()).append("minor", this.getMinor()).append("update", this.getUpdate()).append("beta", this.isBeta()).append("ea", this.isEarlyAccess()).append("release", this.isRelease()).build();
   }

   public static JavaVersion create(int epoch, int major, int minor, int update) {
      return new JavaVersion(epoch + "." + major + "." + minor + (update > 0 ? "_" + update : ""), (String)null, epoch, major, minor, update);
   }

   public static JavaVersion parse(String version) {
      Matcher matcher = pattern.matcher(StringUtil.requireNotBlank(version, "version"));
      if (!matcher.matches()) {
         throw new IllegalArgumentException("could not parse java version");
      } else if (matcher.groupCount() != 5) {
         throw new IllegalArgumentException("illegal group count: " + matcher.groupCount());
      } else {
         return new JavaVersion(version, matcher.group(5), parse(matcher.group(1), "epoch"), parse(matcher.group(2), "major"), parse(matcher.group(3), "minor"), parse(matcher.group(4), "update", true));
      }
   }

   private static int parse(String str, String name, boolean zeroifNull) {
      RuntimeException nested = null;
      if (StringUtils.isEmpty(str)) {
         if (zeroifNull) {
            return 0;
         }
      } else {
         try {
            return Integer.parseInt(str);
         } catch (RuntimeException var5) {
            nested = var5;
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
      } else {
         return num;
      }
   }

   private static int ifNotNegative(int num, String name) {
      if (num < 0) {
         throw new IllegalArgumentException(name + " must not be negative");
      } else {
         return num;
      }
   }

   private static int ifNotSmallerMinusOne(int num, String name) {
      if (num < -1) {
         throw new IllegalArgumentException(name + " must not be less than -1");
      } else {
         return num;
      }
   }
}
