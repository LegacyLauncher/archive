package ru.turikhay.util;

import static ru.turikhay.util.Range.RangeDifference.FITS;
import static ru.turikhay.util.Range.RangeDifference.GREATER;
import static ru.turikhay.util.Range.RangeDifference.LESS;

public class Range<T extends Number> {
	private final T minValue, maxValue;
	private final boolean including;

	private final double doubleMin, doubleMax;

	public Range(T minValue, T maxValue, boolean including) {
		if(minValue == null)
			throw new NullPointerException("min");

		if(maxValue == null)
			throw new NullPointerException("max");

		this.minValue = minValue;
		this.maxValue = maxValue;

		this.doubleMin = minValue.doubleValue();
		this.doubleMax = maxValue.doubleValue();

		if(doubleMin >= doubleMax)
			throw new IllegalArgumentException("min >= max");

		this.including = including;
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

	public RangeDifference getDifference(T value) {
		if(value == null)
			throw new NullPointerException("value");

		double doubleValue = value.doubleValue();
		double min = doubleValue - doubleMin;

		if(min == 0d) // value = minValue
			return including? FITS : LESS;

		if(min < 0d) // value < minValue
			return LESS;

		double max = doubleValue - doubleMax;

		if(max == 0d) // value = maxValue
			return including? FITS : GREATER;

		if(max > 0d) // value > maxValue
			return GREATER;

		return FITS;
	}

	public boolean fits(T value) {
		return getDifference(value) == FITS;
	}

	public enum RangeDifference {
		LESS, FITS, GREATER;
	}
}
