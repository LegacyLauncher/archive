package com.turikhay.util;

import com.turikhay.exceptions.ParseException;

public class IntegerArray {
	public final static char defaultDelimiter = ';';

	private final int[] integers;
	private final char delimiter;
	private final int length;

	private IntegerArray(char del, int... values) {
		delimiter = del;
		length = values.length;
		integers = new int[length];
		System.arraycopy(values, 0, integers, 0, length);
	}

	public IntegerArray(int... values) {
		this(defaultDelimiter, values);
	}

	public int get(int pos) {
		if (pos < 0 || pos >= length)
			throw new ArrayIndexOutOfBoundsException("Invalid position (" + pos
					+ " / " + length + ")!");

		return integers[pos];
	}

	public void set(int pos, int val) {
		if (pos < 0 || pos >= length)
			throw new ArrayIndexOutOfBoundsException("Invalid position (" + pos
					+ " / " + length + ")!");

		integers[pos] = val;
	}

	public int[] toArray() {
		int[] r = new int[length];

		System.arraycopy(integers, 0, r, 0, length);
		return r;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;

		for (int i : integers) {
			if (!first)
				sb.append(delimiter);
			else
				first = false;
			sb.append(i);
		}

		return sb.toString();
	}

	private static IntegerArray parseIntegerArray(String val, char del)
			throws ParseException {
		if (val == null)
			throw new ParseException("String cannot be NULL!");

		if (val.length() <= 1)
			throw new ParseException(
					"String mustn't equal or be less than delimiter!");

		String[] ints = val.split("(?<!\\\\)\\" + del);
		int l = ints.length, cur;
		int[] arr = new int[l];
		for (int i = 0; i < l; i++) {
			try {
				cur = Integer.parseInt(ints[i]);
			} catch (NumberFormatException e) {
				U.log("Cannot parse integer (iteration: " + i + ")", e);
				throw new ParseException("Cannot parse integer (iteration: "
						+ i + ")", e);
			}

			arr[i] = cur;
		}

		return new IntegerArray(del, arr);
	}

	public static IntegerArray parseIntegerArray(String val)
			throws ParseException {
		return parseIntegerArray(val, defaultDelimiter);
	}

	private static int[] toArray(String val, char del) throws ParseException {
		IntegerArray arr = parseIntegerArray(val, del);
		return arr.toArray();
	}

	public static int[] toArray(String val) throws ParseException {
		return toArray(val, defaultDelimiter);
	}

}
