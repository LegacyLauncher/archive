package ru.turikhay.util;

public class Range {
   private final Number minValue;
   private final Number maxValue;
   private final boolean including;
   private final double doubleMin;
   private final double doubleMax;

   public Range(Number minValue, Number maxValue, boolean including) {
      if (minValue == null) {
         throw new NullPointerException("min");
      } else if (maxValue == null) {
         throw new NullPointerException("max");
      } else {
         this.minValue = minValue;
         this.maxValue = maxValue;
         this.doubleMin = minValue.doubleValue();
         this.doubleMax = maxValue.doubleValue();
         if (this.doubleMin >= this.doubleMax) {
            throw new IllegalArgumentException("min >= max");
         } else {
            this.including = including;
         }
      }
   }

   public Range(Number minValue, Number maxValue) {
      this(minValue, maxValue, true);
   }

   public Number getMinValue() {
      return this.minValue;
   }

   public Number getMaxValue() {
      return this.maxValue;
   }

   public boolean getIncluding() {
      return this.including;
   }

   public Range.RangeDifference getDifference(Number value) {
      if (value == null) {
         throw new NullPointerException("value");
      } else {
         double doubleValue = value.doubleValue();
         double min = doubleValue - this.doubleMin;
         if (min == 0.0D) {
            return this.including ? Range.RangeDifference.FITS : Range.RangeDifference.LESS;
         } else if (min < 0.0D) {
            return Range.RangeDifference.LESS;
         } else {
            double max = doubleValue - this.doubleMax;
            return max == 0.0D ? (this.including ? Range.RangeDifference.FITS : Range.RangeDifference.GREATER) : (max > 0.0D ? Range.RangeDifference.GREATER : Range.RangeDifference.FITS);
         }
      }
   }

   public boolean fits(Number value) {
      return this.getDifference(value) == Range.RangeDifference.FITS;
   }

   public static enum RangeDifference {
      LESS,
      FITS,
      GREATER;
   }
}
