package ru.turikhay.util;

import java.util.Hashtable;
import java.util.Map;

public class Time {
	private static Map<Object, Long> timers = new Hashtable<Object, Long>();

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
}
