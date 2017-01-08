package ru.turikhay.util;

public class Range<T extends Number> {
    private final T minValue;
    private final T maxValue;
    private final boolean including;
    private final double doubleMin;
    private final double doubleMax;

    public Range(T minValue, T maxValue, boolean including) {
        if (minValue == null) {
            throw new NullPointerException("min");
        } else if (maxValue == null) {
            throw new NullPointerException("max");
        } else {
            this.minValue = minValue;
            this.maxValue = maxValue;
            doubleMin = minValue.doubleValue();
            doubleMax = maxValue.doubleValue();
            if (doubleMin >= doubleMax) {
                throw new IllegalArgumentException("min >= max");
            } else {
                this.including = including;
            }
        }
    }

    public Range(T minValue, T maxValue) {
        this(minValue, maxValue, true);
    }

    public T getMinValue() {
        return minValue;
    }

    public T getMaxValue() {
        return maxValue;
    }

    public boolean getIncluding() {
        return including;
    }

    public Range.RangeDifference getDifference(T value) {
        if (value == null) {
            throw new NullPointerException("value");
        } else {
            double doubleValue = value.doubleValue();
            double min = doubleValue - doubleMin;
            if (min == 0.0D) {
                return including ? Range.RangeDifference.FITS : Range.RangeDifference.LESS;
            } else if (min < 0.0D) {
                return Range.RangeDifference.LESS;
            } else {
                double max = doubleValue - doubleMax;
                return max == 0.0D ? (including ? Range.RangeDifference.FITS : Range.RangeDifference.GREATER) : (max > 0.0D ? Range.RangeDifference.GREATER : Range.RangeDifference.FITS);
            }
        }
    }

    public boolean fits(T value) {
        return getDifference(value) == Range.RangeDifference.FITS;
    }

    public enum RangeDifference {
        LESS,
        FITS,
        GREATER
    }
}
