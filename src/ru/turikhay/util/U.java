package ru.turikhay.util;

import java.awt.Color;
import java.io.Closeable;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import ru.turikhay.tlauncher.Bootstrapper.LoadingStep;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration.ConnectionQuality;
import ru.turikhay.util.async.ExtendedThread;

public class U {
	public final static String PROGRAM_PACKAGE = "ru.turikhay";
	public final static int DEFAULT_CONNECTION_TIMEOUT = 15000;

	private final static int ST_TOTAL = 100, ST_PROGRAM = 10;
	private static String PREFIX;

	private U() {
	}

	public static void setPrefix(String prefix) {
		PREFIX = prefix;
	}

	public static String getPrefix() {
		return PREFIX;
	}

	private static final Object lock = new Object();

	public static void linelog(Object what) {
		synchronized (lock) {
			System.out.print(what);
		}
	}

	/**
	 * Logs with default prefix
	 */
	public static void log(Object... what) {
		hlog(PREFIX, what);
	}

	/**
	 * Logs without prefix
	 */
	public static void plog(Object... what) {
		hlog(null, what);
	}

	private static void hlog(String prefix, Object[] append) {
		synchronized (lock) {
			System.out.println(toLog(prefix, append));
		}
	}

	private static String toLog(String prefix, Object... append) {
		StringBuilder b = new StringBuilder();
		boolean first = true;

		if (prefix != null) {
			b.append(prefix);
			first = false;
		}

		if (append != null)
			for (Object e : append) {
				if(e != null)
					if (e.getClass().isArray()) {
						if (!first)
							b.append(" ");

						if (e instanceof Object[])
							b.append(toLog((Object[]) e));
						else
							b.append(arrayToLog(e));

						continue;
					}
					else if (e instanceof Throwable) {
						if (!first)
							b.append("\n");
						b.append(stackTrace((Throwable) e));
						b.append("\n");

						continue;
					}
					else if (e instanceof File) {
						if (!first)
							b.append(" ");
						File file = (File) e;
						String absPath = file.getAbsolutePath();

						b.append(absPath);

						if(file.isDirectory() && !absPath.endsWith(File.separator))
							b.append(File.separator);
					} else if (e instanceof Iterator) {
						Iterator<?> i = (Iterator<?>) e;
						while (i.hasNext()) {
							b.append(" ");
							b.append(toLog(i.next()));
						}
					} else if (e instanceof Enumeration) {
						Enumeration<?> en = (Enumeration<?>) e;
						while(en.hasMoreElements()) {
							b.append(" ");
							b.append(toLog(en.nextElement()));
						}
					} else {
						if (!first)
							b.append(" ");
						b.append(e);
					}
				else {
					if(!first)
						b.append(" ");
					b.append("null");
				}

				if (first)
					first = false;
			}
		else
			b.append("null");

		return b.toString();
	}

	public static String toLog(Object... append) {
		return toLog(null, append);
	}

	private static String arrayToLog(Object e) {
		if (!e.getClass().isArray())
			throw new IllegalArgumentException("Given object is not an array!");

		StringBuilder b = new StringBuilder();
		boolean first = true;

		if (e instanceof Object[])
			for (Object i : (Object[]) e) {
				if (!first)
					b.append(" ");
				else
					first = false;
				b.append(i);
			}
		else if (e instanceof int[])
			for (int i : (int[]) e) {
				if (!first)
					b.append(" ");
				else
					first = false;
				b.append(i);
			}
		else if (e instanceof boolean[])
			for (boolean i : (boolean[]) e) {
				if (!first)
					b.append(" ");
				else
					first = false;
				b.append(i);
			}
		else if (e instanceof long[])
			for (long i : (long[]) e) {
				if (!first)
					b.append(" ");
				else
					first = false;
				b.append(i);
			}
		else if (e instanceof float[])
			for (float i : (float[]) e) {
				if (!first)
					b.append(" ");
				else
					first = false;
				b.append(i);
			}
		else if (e instanceof double[])
			for (double i : (double[]) e) {
				if (!first)
					b.append(" ");
				else
					first = false;
				b.append(i);
			}
		else if (e instanceof byte[])
			for (byte i : (byte[]) e) {
				if (!first)
					b.append(" ");
				else
					first = false;
				b.append(i);
			}
		else if (e instanceof short[])
			for (short i : (short[]) e) {
				if (!first)
					b.append(" ");
				else
					first = false;
				b.append(i);
			}
		else if (e instanceof char[])
			for (char i : (char[]) e) {
				if (!first)
					b.append(" ");
				else
					first = false;
				b.append(i);
			}

		if (b.length() == 0)
			throw new UnknownError("Unknown array type given.");

		return b.toString();
	}

	public static void setLoadingStep(LoadingStep step) {
		if(step == null)
			throw new NullPointerException();

		plog(LoadingStep.LOADING_PREFIX, step.toString());

	}

	public static short shortRandom() {
		return (short) new Random(System.currentTimeMillis())
		.nextInt(Short.MAX_VALUE);
	}

	public static double doubleRandom() {
		return new Random(System.currentTimeMillis()).nextDouble();
	}

	public static int random(int s, int e) {
		return new Random(System.currentTimeMillis()).nextInt(e - s) + s;
	}

	public static boolean ok(int d) {
		return new Random(System.currentTimeMillis()).nextInt(d) == 0;
	}

	public static double getAverage(double[] d) {
		double a = 0;
		int k = 0;

		for (double curd : d) {
			if (curd == 0)
				continue;
			a += curd;
			++k;
		}

		if (k == 0)
			return 0;
		return a / k;
	}

	public static double getAverage(double[] d, int max) {
		double a = 0;
		int k = 0;

		for (double curd : d) {
			a += curd;
			++k;
			if (k == max)
				break;
		}

		if (k == 0)
			return 0;
		return a / k;
	}

	public static int getAverage(int[] d) {
		int a = 0, k = 0;

		for (int curd : d) {
			if (curd == 0)
				continue;
			a += curd;
			++k;
		}

		if (k == 0)
			return 0;
		return Math.round(a / k);
	}

	public static int getAverage(int[] d, int max) {
		int a = 0, k = 0;

		for (int curd : d) {
			a += curd;
			++k;
			if (k == max)
				break;
		}

		if (k == 0)
			return 0;
		return Math.round(a / k);
	}

	public static int getSum(int[] d) {
		int a = 0;

		for (int curd : d)
			a += curd;
		return a;
	}

	public static double getSum(double[] d) {
		double a = 0;

		for (double curd : d)
			a += curd;
		return a;
	}

	public static int getMaxMultiply(int i, int max) {
		if (i <= max)
			return 1;
		for (int x = max; x > 1; x--)
			if (i % x == 0)
				return x;
		return (int) Math.ceil(i / max);
	}

	@Deprecated
	public static String r(String string, int max) {
		if (string == null)
			return null;

		int len = string.length();
		if (len <= max)
			return string;

		String[] words = string.split(" ");
		String ret = "";
		int remaining = max + 1;

		for (int x = 0; x < words.length; x++) {
			String curword = words[x];
			int curlen = curword.length();

			if (curlen < remaining) {
				ret += " " + curword;
				remaining -= curlen + 1;

				continue;
			}

			if (x == 0)
				ret += " " + curword.substring(0, remaining - 1);
			break;
		}

		if (ret.length() == 0)
			return "";
		return ret.substring(1) + "...";
	}

	public static String setFractional(double d, int fractional) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(fractional);

		return nf.format(d).replace(",", ".");
	}

	private static String stackTrace(Throwable e) {
		StringBuilder trace = rawStackTrace(e);

		ExtendedThread currentAsExtended = U.getAs(Thread.currentThread(),
				ExtendedThread.class);
		if (currentAsExtended != null)
			trace.append("\nThread called by: ").append(
					rawStackTrace(currentAsExtended.getCaller()));

		return trace.toString();
	}

	private static StringBuilder rawStackTrace(Throwable e) {
		if (e == null)
			return null;

		StackTraceElement[] elems = e.getStackTrace();
		int programElements = 0, totalElements = 0;

		StringBuilder builder = new StringBuilder();
		builder.append(e.toString());

		for (StackTraceElement elem : elems) {
			++totalElements;

			String description = elem.toString();

			if (description.startsWith(PROGRAM_PACKAGE))
				++programElements;

			builder.append("\nat ").append(description);

			if (totalElements == ST_TOTAL || programElements == ST_PROGRAM) {
				// Stack trace limit exceed
				int remain = (elems.length - totalElements);

				if (remain != 0)
					builder.append("\n... and ").append(remain).append(" more");

				break;
			}
		}

		Throwable cause = e.getCause();
		if (cause != null)
			builder.append("\nCaused by: ").append(rawStackTrace(cause));

		return builder;
	}

	public static long getUsingSpace() {
		return U.getTotalSpace() - U.getFreeSpace();
	}

	public static long getFreeSpace() {
		return (Runtime.getRuntime().freeMemory() / (1024 * 1024));
	}

	public static long getTotalSpace() {
		return (Runtime.getRuntime().totalMemory() / (1024 * 1024));
	}

	public static String memoryStatus() {
		return getUsingSpace() + " / " + getTotalSpace() + " MB";
	}

	public static void gc() {
		log("Starting garbage collector: " + memoryStatus());
		System.gc();
		log("Garbage collector completed: " + memoryStatus());
	}

	public static void sleepFor(long millis) {
		try {
			Thread.sleep(millis);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static URL makeURL(String p) {
		try {
			return new URL(p);
		} catch (Exception e) {
			// U.log("Cannot make URL from string: "+p+".", e);
		}

		return null;
	}

	public static URI makeURI(URL url) {
		try {
			return url.toURI();
		} catch (Exception e) {
			// U.log("Cannot make URI from URL: "+url+".", e);
		}

		return null;
	}

	public static URI makeURI(String p) {
		return U.makeURI(U.makeURL(p));
	}

	private static boolean interval(int min, int max, int num, boolean including) {
		return (including) ? (num >= min && num <= max)
				: (num > max && num < max);
	}

	public static boolean interval(int min, int max, int num) {
		return interval(min, max, num, true);
	}

	public static int fitInterval(int val, int min, int max) {
		if (val > max)
			return max;
		if (val < min)
			return min;
		return val;
	}

	public static long m() {
		return System.currentTimeMillis();
	}

	public static long n() {
		return System.nanoTime();
	}

	public static int getReadTimeout() {
		return getConnectionTimeout();
	}

	public static int getConnectionTimeout() {
		TLauncher t = TLauncher.getInstance();
		if (t == null)
			return DEFAULT_CONNECTION_TIMEOUT;

		ConnectionQuality quality = t.getSettings().getConnectionQuality();
		if (quality == null)
			return DEFAULT_CONNECTION_TIMEOUT;

		return quality.getTimeout();
	}

	public static <T> T getRandom(T[] array) {
		if (array == null)
			return null;
		if (array.length == 0)
			return null;
		if (array.length == 1)
			return array[0];

		return array[new Random().nextInt(array.length)];
	}

	public static <K, E> LinkedHashMap<K, E> sortMap(Map<K, E> map,
			K[] sortedKeys) {
		if (map == null)
			return null;

		if (sortedKeys == null)
			throw new NullPointerException("Keys cannot be NULL!");

		LinkedHashMap<K, E> result = new LinkedHashMap<K, E>();

		for (K key : sortedKeys) {
			for (Entry<K, E> entry : map.entrySet()) {
				K entryKey = entry.getKey();
				E value = entry.getValue();

				if (key == null && entryKey == null) {
					result.put(null, value);
					break;
				}

				if (key == null)
					continue;
				if (!key.equals(entryKey))
					continue;

				result.put(key, value);
				break;
			}
		}

		return result;
	}

	public static Color randomColor() {
		Random random = new Random();
		return new Color(random.nextInt(256), random.nextInt(256),
				random.nextInt(256));
	}

	public static Color shiftColor(Color color, int bits) {
		if (color == null)
			return null;

		if (bits == 0)
			return color;

		int newRed = fitInterval(color.getRed() + bits, 0, 255), newGreen = fitInterval(
				color.getGreen() + bits, 0, 255), newBlue = fitInterval(
						color.getBlue() + bits, 0, 255);

		return new Color(newRed, newGreen, newBlue, color.getAlpha());
	}

	public static Color shiftAlpha(Color color, int bits) {
		if(color == null)
			return null;

		if(bits == 0)
			return color;

		int newAlpha = fitInterval(color.getAlpha() + bits, 0 , 255);

		return new Color(color.getRed(), color.getGreen(), color.getBlue(), newAlpha);
	}

	@Deprecated
	public static <T> T getAs(Object o, Class<T> classOfT) {
		return Reflect.cast(o, classOfT);
	}

	public static boolean equal(Object a, Object b) {
		if(a == b)
			return true;

		if(a != null)
			return a.equals(b);

		return false;
	}

	public static String[] extend(String[] a0, String[] a1) {
		String[] newArray = new String[a0.length + a1.length];

		System.arraycopy(a0, 0, newArray, 0, a0.length);
		System.arraycopy(a1, 0, newArray, a0.length, a1.length);

		return newArray;
	}

	public static void close(Closeable c) {
		try { c.close(); }
		catch(Throwable e) {
			e.printStackTrace();
		}
	}

	public static <T> boolean is(T obj, T... equal) {
		if(equal == null)
			throw new NullPointerException("comparsion array");

		if(obj == null) {
			for(T compare : equal)
				if(compare == null)
					return true;
		} else {
			for(T compare : equal)
				if(obj.equals(compare))
					return true;
		}

		return false;
	}

	public static <T> int find(T obj, T[] array) {
		if(obj == null) {
			for(int i=0;i<array.length;i++)
				if(array[i] == null)
					return i;
		} else {
			for(int i=0;i<array.length;i++)
				if(obj.equals(array[i]))
					return i;
		}

		return -1;
	}
}
