package com.turikhay.util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class Time {
	private static final String defaultFormat = "[day.month hour:minute]";
	private static Map<Object, Long> timers = new HashMap<Object, Long>();

	// 21600000
	private int offset;

	public Time(int rawOffset) {
		int timezoneOffset = TimeZone.getDefault().getRawOffset()
				+ TimeZone.getDefault().getDSTSavings();

		offset = rawOffset - timezoneOffset;
	}

	public Time(TimeZone timezone) {
		this(timezone.getRawOffset());
	}

	long current() {
		return System.currentTimeMillis() + offset;
	}

	public long seccurrent() {
		return Math.round(current() / 1000);
	}

	public String presentDate(Calendar cal) {
		return presentDate(cal, defaultFormat);
	}

	public String presentDate(String format) {
		return presentDate(current(), format);
	}

	public String presentDate() {
		return presentDate(current());
	}

	public int[] unix(long unixtime) {
		Calendar ca = Calendar.getInstance();
		ca.setTimeZone(TimeZone.getTimeZone("UTC"));
		ca.setTimeInMillis(unixtime);
		int[] toret = new int[5];

		toret[0] = ca.get(Calendar.WEEK_OF_YEAR) - 1;
		toret[1] = ca.get(Calendar.DAY_OF_WEEK) - 5;
		toret[2] = ca.get(Calendar.HOUR_OF_DAY);
		toret[3] = ca.get(Calendar.MINUTE);
		toret[4] = ca.get(Calendar.SECOND);
		return toret;
	}

	public static String presentDate(Calendar cal, String format) {
		return format.replaceAll("day", zero(cal.get(Calendar.DAY_OF_MONTH)))
				.replaceAll("month", zero(cal.get(Calendar.MONTH) + 1))
				.replaceAll("year", cal.get(Calendar.YEAR) + "")
				.replaceAll("hour", zero(cal.get(Calendar.HOUR_OF_DAY)))
				.replaceAll("minute", zero(cal.get(Calendar.MINUTE)))
				.replaceAll("second", zero(cal.get(Calendar.SECOND)));
	}

	public static String presentDate(long unix, String format) {
		Calendar p = Calendar.getInstance();
		p.setTimeInMillis(unix);

		return presentDate(p, format);
	}

	public static String presentDate(long unix) {
		Calendar p = Calendar.getInstance();
		p.setTimeInMillis(unix);

		return presentDate(p, defaultFormat);
	}

	public static void start(Object holder) {
		if (timers.containsKey(holder))
			throw new IllegalArgumentException("This holder ("
					+ holder.toString() + ") is already in use!");

		timers.put(holder, System.currentTimeMillis());
	}

	public static long stop(Object holder) {
		long current = System.currentTimeMillis();

		Long l = timers.get(holder);

		if (l == null)
			return 0;

		timers.remove(holder);
		return current - l;
	}

	public static void start() {
		start(Thread.currentThread());
	}

	public static long stop() {
		return stop(Thread.currentThread());
	}

	private static String zero(int integer) {
		if (integer < 10)
			return "0" + integer;
		return integer + "";
	}
}
