package ru.turikhay.util;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.async.ExtendedThread;

import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

public class U {
    private static final Logger LOGGER = LogManager.getLogger();

    private static String toLog(String prefix, Object... append) {
        StringBuilder b = new StringBuilder();
        boolean first = true;
        if (prefix != null) {
            b.append(prefix);
            first = false;
        }

        if (append != null) {
            for (Object e : append) {
                if (e != null) {
                    if (e.getClass().isArray()) {
                        if (!first) {
                            b.append(" ");
                        }

                        if (e instanceof Object[]) {
                            b.append(toLog((Object[]) e));
                        } else {
                            b.append(arrayToLog(e));
                        }
                        continue;
                    }

                    if (e instanceof Throwable) {
                        if (!first) {
                            b.append("\n");
                        }

                        b.append(stackTrace((Throwable) e));
                        b.append("\n");
                        continue;
                    }

                    if (e instanceof File) {
                        if (!first) {
                            b.append(" ");
                        }

                        File en = (File) e;
                        String absPath = en.getAbsolutePath();
                        b.append(absPath);
                        if (en.isDirectory() && !absPath.endsWith(File.separator)) {
                            b.append(File.separator);
                        }
                    } else if (e instanceof Iterator) {
                        Iterator<?> var10 = (Iterator<?>) e;

                        while (var10.hasNext()) {
                            b.append(" ");
                            b.append(toLog(var10.next()));
                        }
                    } else if (e instanceof Enumeration) {
                        Enumeration<?> var11 = (Enumeration<?>) e;

                        while (var11.hasMoreElements()) {
                            b.append(" ");
                            b.append(toLog(var11.nextElement()));
                        }
                    } else {
                        if (!first) {
                            b.append(" ");
                        }

                        b.append(e);
                    }
                } else {
                    if (!first) {
                        b.append(" ");
                    }

                    b.append("null");
                }

                if (first) {
                    first = false;
                }
            }
        } else {
            b.append("null");
        }

        return b.toString();
    }

    public static String toLog(Object... append) {
        return toLog(null, append);
    }

    private static String arrayToLog(Object e) {
        if (!e.getClass().isArray()) {
            throw new IllegalArgumentException("Given object is not an array!");
        } else {
            StringBuilder b = new StringBuilder();
            boolean first = true;
            int var4;
            int var5;
            if (e instanceof Object[]) {
                Object[] var6;
                var5 = (var6 = (Object[]) e).length;

                for (var4 = 0; var4 < var5; ++var4) {
                    Object i = var6[var4];
                    if (!first) {
                        b.append(" ");
                    } else {
                        first = false;
                    }

                    b.append(i);
                }
            } else if (e instanceof int[]) {
                int[] var16;
                var5 = (var16 = (int[]) e).length;

                for (var4 = 0; var4 < var5; ++var4) {
                    int var8 = var16[var4];
                    if (!first) {
                        b.append(" ");
                    } else {
                        first = false;
                    }

                    b.append(var8);
                }
            } else if (e instanceof boolean[]) {
                boolean[] var17;
                var5 = (var17 = (boolean[]) e).length;

                for (var4 = 0; var4 < var5; ++var4) {
                    boolean var9 = var17[var4];
                    if (!first) {
                        b.append(" ");
                    } else {
                        first = false;
                    }

                    b.append(var9);
                }
            } else {
                int var18;
                if (e instanceof long[]) {
                    long[] var7;
                    var18 = (var7 = (long[]) e).length;

                    for (var5 = 0; var5 < var18; ++var5) {
                        long var10 = var7[var5];
                        if (!first) {
                            b.append(" ");
                        } else {
                            first = false;
                        }

                        b.append(var10);
                    }
                } else if (e instanceof float[]) {
                    float[] var19;
                    var5 = (var19 = (float[]) e).length;

                    for (var4 = 0; var4 < var5; ++var4) {
                        float var11 = var19[var4];
                        if (!first) {
                            b.append(" ");
                        } else {
                            first = false;
                        }

                        b.append(var11);
                    }
                } else if (e instanceof double[]) {
                    double[] var20;
                    var18 = (var20 = (double[]) e).length;

                    for (var5 = 0; var5 < var18; ++var5) {
                        double var12 = var20[var5];
                        if (!first) {
                            b.append(" ");
                        } else {
                            first = false;
                        }

                        b.append(var12);
                    }
                } else if (e instanceof byte[]) {
                    byte[] var21;
                    var5 = (var21 = (byte[]) e).length;

                    for (var4 = 0; var4 < var5; ++var4) {
                        byte var13 = var21[var4];
                        if (!first) {
                            b.append(" ");
                        } else {
                            first = false;
                        }

                        b.append(var13);
                    }
                } else if (e instanceof short[]) {
                    short[] var22;
                    var5 = (var22 = (short[]) e).length;

                    for (var4 = 0; var4 < var5; ++var4) {
                        short var14 = var22[var4];
                        if (!first) {
                            b.append(" ");
                        } else {
                            first = false;
                        }

                        b.append(var14);
                    }
                } else if (e instanceof char[]) {
                    char[] var23;
                    var5 = (var23 = (char[]) e).length;

                    for (var4 = 0; var4 < var5; ++var4) {
                        char var15 = var23[var4];
                        if (!first) {
                            b.append(" ");
                        } else {
                            first = false;
                        }

                        b.append(var15);
                    }
                }
            }

            if (b.length() == 0) {
                throw new UnknownError("Unknown array type given.");
            } else {
                return b.toString();
            }
        }
    }

    public static double getAverage(double[] d, int max) {
        double a = 0;
        int k = 0;

        for (double curd : d) {
            a += curd;
            ++k;
            if (k == max) break;
        }

        if (k == 0) return 0;
        return a / k;
    }


    public static int getMaxMultiply(int i, int max) {
        if (i <= max) return 1;
        for (int x = max; x > 1; x--)
            if (i % x == 0) return x;
        return (int) Math.ceil(i / max);
    }

    public static StringBuilder stackTrace(Throwable e) {
        StringBuilder trace = rawStackTrace(e);
        Thread thread = Thread.currentThread();
        if (thread instanceof ExtendedThread) {
            trace.append("\nThread called by: ").append(rawStackTrace(((ExtendedThread) thread).getCaller()));
        }

        return trace;
    }

    private static StringBuilder rawStackTrace(Throwable e) {
        if (e == null) {
            return null;
        } else {
            StackTraceElement[] elems = e.getStackTrace();
            int programElements = 0;
            int totalElements = 0;
            StringBuilder builder = new StringBuilder();
            builder.append(e);

            for (StackTraceElement cause : elems) {
                ++totalElements;
                String description = cause.toString();
                if (description.startsWith("ru.turikhay")) {
                    ++programElements;
                }

                builder.append("\nat ").append(description);
                if (totalElements == 100 || programElements == 50) {
                    int remain = elems.length - totalElements;
                    if (remain != 0) {
                        builder.append("\n... and ").append(remain).append(" more");
                    }
                    break;
                }
            }

            Throwable var11 = e.getCause();
            if (var11 != null) {
                builder.append("\nCaused by: ").append(rawStackTrace(var11));
            }

            return builder;
        }
    }

    public static long getUsingSpace() {
        return getTotalSpace() - getFreeSpace();
    }

    public static long getFreeSpace() {
        return Runtime.getRuntime().freeMemory() / 1048576L;
    }

    public static long getTotalSpace() {
        return Runtime.getRuntime().totalMemory() / 1048576L;
    }

    public static String memoryStatus() {
        return getUsingSpace() + " / " + getTotalSpace() + " MB";
    }

    public static void sleepFor(long millis, boolean throwIfInterrupted) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            if (throwIfInterrupted) {
                throw new RuntimeException(e);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void sleepFor(long millis) {
        sleepFor(millis, true);
    }

    public static URL makeURL(String p, boolean assertValid) {
        try {
            return new URL(p);
        } catch (MalformedURLException e) {
            if (assertValid) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static URL makeURL(String p) {
        return makeURL(p, false);
    }

    public static int clamp(int val, int min, int max) {
        return Math.min(max, Math.max(val, min));
    }

    public static int getReadTimeout() {
        return getConnectionTimeout();
    }

    public static int getConnectionTimeout() {
        return 15000;
    }

    public static Proxy getProxy() {
        return Proxy.NO_PROXY;
    }

    public static <T> T getRandom(List<? extends T> col) {
        if (col == null || col.size() == 0) return null;
        return col.size() == 1 ? col.get(0) : col.get(ThreadLocalRandom.current().nextInt(col.size()));
    }

    public static <K, E> Map<K, E> sortMap(Map<K, E> map, K[] sortedKeys) {
        if (map == null) {
            return null;
        } else if (sortedKeys == null) {
            throw new NullPointerException("Keys cannot be NULL!");
        } else {
            Map<K, E> result = new LinkedHashMap<>();

            for (K key : sortedKeys) {
                for (Entry<K, E> entry : map.entrySet()) {
                    K entryKey = entry.getKey();
                    E value = entry.getValue();
                    if (key == null && entryKey == null) {
                        result.put(null, value);
                        break;
                    }

                    if (key != null && key.equals(entryKey)) {
                        result.put(key, value);
                        break;
                    }
                }
            }

            return result;
        }
    }

    public static Color shiftColor(Color color, int bits, int min, int max) {
        if (color == null) {
            return null;
        } else if (bits == 0) {
            return color;
        } else {
            int newRed = clamp(color.getRed() + bits, min, max);
            int newGreen = clamp(color.getGreen() + bits, min, max);
            int newBlue = clamp(color.getBlue() + bits, min, max);
            return new Color(newRed, newGreen, newBlue, color.getAlpha());
        }
    }

    public static Color shiftColor(Color color, int bits) {
        return shiftColor(color, bits, 0, 255);
    }

    public static Color shiftAlpha(Color color, int bits, int min, int max) {
        if (color == null) {
            return null;
        } else if (bits == 0) {
            return color;
        } else {
            int newAlpha = clamp(color.getAlpha() + bits, min, max);
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), newAlpha);
        }
    }

    public static Color shiftAlpha(Color color, int bits) {
        return shiftAlpha(color, bits, 0, 255);
    }

    public static Locale getLocale(String tag) {
        if (tag == null) {
            return null;
        }
        try {
            return LocaleUtils.toLocale(tag);
        } catch (RuntimeException e) {
            LOGGER.warn("Couldn't parse locale: {}", tag, e);
            return null;
        }
    }

    public static Calendar getUTC() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    }

    public static Calendar getUTC(long millis) {
        Calendar date = getUTC();
        date.setTimeInMillis(millis);
        return date;
    }

    public static String getNormalVersion(Version version) {
        return String.format(java.util.Locale.ROOT,
                "%d.%d.%d",
                version.getMajorVersion(),
                version.getMinorVersion(),
                version.getPatchVersion()
        );
    }

    public static String getFormattedVersion(Version version) {
        StringBuilder sb = new StringBuilder(getNormalVersion(version));
        if (!version.getPreReleaseVersion().isEmpty()) {
            sb.append("-").append(version.getPreReleaseVersion());
        }
        if (!version.getBuildMetadata().isEmpty()) {
            sb.append("+").append(version.getBuildMetadata());
        }
        return sb.toString();
    }

    public static String getMinorVersion(Version version) {
        return String.format(java.util.Locale.ROOT,
                "%s%d.%d",
                version.getMajorVersion() == 1 ? "" : version.getMajorVersion() + ".",
                version.getMinorVersion(),
                version.getPatchVersion()
        );
    }

    public static String parseHost(String url) {
        if (url == null) {
            return null;
        }
        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private static final Lazy<Gson> gson = Lazy.of(() -> new GsonBuilder().create());

    public static Gson getGson() {
        return gson.get();
    }
}
