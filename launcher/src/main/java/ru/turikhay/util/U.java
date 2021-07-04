package ru.turikhay.util;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.async.ExtendedThread;
import ru.turikhay.util.stream.InputStringStream;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

public class U {
    public static final String PROGRAM_PACKAGE = "ru.turikhay";
    public static final int DEFAULT_CONNECTION_TIMEOUT = 15000;

    private static final Logger LOGGER = LogManager.getLogger();

    private static String toLog(String prefix, Object... append) {
        StringBuilder b = new StringBuilder();
        boolean first = true;
        if (prefix != null) {
            b.append(prefix);
            first = false;
        }

        if (append != null) {
            Object[] var7 = append;
            int var6 = append.length;

            for (int var5 = 0; var5 < var6; ++var5) {
                Object e = var7[var5];
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
                        Iterator var10 = (Iterator) e;

                        while (var10.hasNext()) {
                            b.append(" ");
                            b.append(toLog(new Object[]{var10.next()}));
                        }
                    } else if (e instanceof Enumeration) {
                        Enumeration var11 = (Enumeration) e;

                        while (var11.hasMoreElements()) {
                            b.append(" ");
                            b.append(toLog(new Object[]{var11.nextElement()}));
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

    public static short shortRandom() {
        return (short) (new Random(System.currentTimeMillis())).nextInt(32767);
    }

    public static double doubleRandom(){
        return new Random(System.currentTimeMillis()).nextDouble();
    }

    public static int random(int s, int e){
        return new Random(System.currentTimeMillis()).nextInt(e - s) + s;
    }

    public static boolean ok(int d){
        return new Random(System.currentTimeMillis()).nextInt(d) == 0;
    }

    public static double getAverage(double[] d) {
        double a = 0.0D;
        int k = 0;
        double[] var8 = d;
        int var7 = d.length;

        for (int var6 = 0; var6 < var7; ++var6) {
            double curd = var8[var6];
            if (curd != 0.0D) {
                a += curd;
                ++k;
            }
        }

        if (k == 0) {
            return 0.0D;
        } else {
            return a / (double) k;
        }
    }

    public static double getAverage(double[] d, int max){
        double a = 0; int k = 0;

        for(double curd : d){
            a += curd; ++k;
            if(k == max) break;
        }

        if(k == 0) return 0;
        return a / k;
    }


    public static int getAverage(int[] d) {
        int a = 0;
        int k = 0;
        int[] var6 = d;
        int var5 = d.length;

        for (int var4 = 0; var4 < var5; ++var4) {
            int curd = var6[var4];
            if (curd != 0) {
                a += curd;
                ++k;
            }
        }

        if (k == 0) {
            return 0;
        } else {
            return Math.round((float) (a / k));
        }
    }

    public static int getAverage(int[] d, int max){
        int a = 0, k = 0;

        for(int curd : d){
            a += curd; ++k;
            if(k == max) break;
        }

        if(k == 0) return 0;
        return Math.round(a / k);
    }


    public static int getSum(int[] d){
        int a = 0;

        for(int curd : d) a += curd;
        return a;
    }

    public static double getSum(double[] d){
        double a = 0;

        for(double curd : d) a += curd;
        return a;
    }


    public static int getMaxMultiply(int i, int max){
        if(i <= max) return 1;
        for(int x=max;x>1;x--)
            if(i % x == 0) return x;
        return (int) Math.ceil(i / max);
    }


    /**
     * @deprecated
     */
    @Deprecated
    public static String r(String string, int max){
        if(string == null) return null;

        int len = string.length();
        if(len <= max) return string;

        String[] words = string.split(" ");
        String ret = ""; int remaining = max + 1;

        for(int x=0;x<words.length;x++){
            String curword = words[x];
            int curlen = curword.length();

            if(curlen < remaining){
                ret += " " + curword;
                remaining -= curlen + 1;

                continue;
            }

            if(x == 0)
                ret += " " + curword.substring(0, remaining - 1);
            break;
        }

        if(ret.length() == 0) return "";
        return ret.substring(1) + "...";
    }


    public static String setFractional(double d, int fractional){
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(fractional);

        return nf.format(d).replace(",", ".");
    }


    public static StringBuilder stackTrace(Throwable e) {
        StringBuilder trace = rawStackTrace(e);
        ExtendedThread currentAsExtended = getAs(Thread.currentThread(), ExtendedThread.class);
        if (currentAsExtended != null) {
            trace.append("\nThread called by: ").append(rawStackTrace(currentAsExtended.getCaller()));
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
            StackTraceElement[] var8 = elems;
            int var7 = elems.length;

            for (int var6 = 0; var6 < var7; ++var6) {
                StackTraceElement cause = var8[var6];
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

    public static void gc() {
        LOGGER.info("Starting garbage collector: " + memoryStatus());
        System.gc();
        LOGGER.info("Garbage collector completed: " + memoryStatus());
    }

    public static void sleepFor(long millis, boolean throwIfInterrupted) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            if (throwIfInterrupted) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void sleepFor(long millis) {
        sleepFor(millis, true);
    }

    public static URL makeURL(String p, boolean assertValid) {
        try {
            return new URL(p);
        } catch (Exception var2) {
            if (assertValid) {
                throw new RuntimeException(var2);
            }
        }
        return null;
    }

    public static URL makeURL(String p) {
        return makeURL(p, false);
    }

    public static URI makeURI(URL url) {
        try {
            return url.toURI();
        } catch (Exception var2) {
            return null;
        }
    }

    public static URI makeURI(String p) {
        return makeURI(makeURL(p));
    }

    private static boolean interval(int min, int max, int num, boolean including) {
        return including ? num >= min && num <= max : num > max && num < max;
    }

    public static boolean interval(int min, int max, int num) {
        return interval(min, max, num, true);
    }

    public static int fitInterval(int val, int min, int max) {
        return val > max ? max : (val < min ? min : val);
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
        return 15000;
    }

    public static Proxy getProxy() {
        return Proxy.NO_PROXY;
    }

    public static <T> T getRandom(T[] array) {
        return array == null ? null : (array.length == 0 ? null : (array.length == 1 ? array[0] : array[(new Random()).nextInt(array.length)]));
    }

    public static <K, E> LinkedHashMap<K, E> sortMap(Map<K, E> map, K[] sortedKeys) {
        if (map == null) {
            return null;
        } else if (sortedKeys == null) {
            throw new NullPointerException("Keys cannot be NULL!");
        } else {
            LinkedHashMap result = new LinkedHashMap();
            Object[] var6 = sortedKeys;
            int var5 = sortedKeys.length;

            for (int var4 = 0; var4 < var5; ++var4) {
                Object key = var6[var4];
                Iterator var8 = map.entrySet().iterator();

                while (var8.hasNext()) {
                    Entry entry = (Entry) var8.next();
                    Object entryKey = entry.getKey();
                    Object value = entry.getValue();
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

    public static Color randomColor() {
        Random random = new Random();
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    public static Color shiftColor(Color color, int bits, int min, int max) {
        if (color == null) {
            return null;
        } else if (bits == 0) {
            return color;
        } else {
            int newRed = fitInterval(color.getRed() + bits, min, max);
            int newGreen = fitInterval(color.getGreen() + bits, min, max);
            int newBlue = fitInterval(color.getBlue() + bits, min, max);
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
            int newAlpha = fitInterval(color.getAlpha() + bits, min, max);
            return new Color(color.getRed(), color.getGreen(), color.getBlue(), newAlpha);
        }
    }

    public static Color shiftAlpha(Color color, int bits) {
        return shiftAlpha(color, bits, 0, 255);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static <T> T getAs(Object o, Class<T> classOfT) {
        return Reflect.cast(o, classOfT);
    }

    public static boolean equal(Object a, Object b) {
        return a == b ? true : (a != null ? a.equals(b) : false);
    }

    public static String[] extend(String[] a0, String[] a1) {
        String[] newArray = new String[a0.length + a1.length];
        System.arraycopy(a0, 0, newArray, 0, a0.length);
        System.arraycopy(a1, 0, newArray, a0.length, a1.length);
        return newArray;
    }

    public static void close(Closeable... c) {
        if (c == null) {
            return;
        }
        for (Closeable close : c) {
            try {
                close.close();
            } catch (Throwable var2) {
            }
        }
    }

    public static <T> boolean is(T obj, T... equal) {
        if (equal == null) {
            throw new NullPointerException("comparsion array");
        } else {
            Object compare;
            int var3;
            int var4;
            Object[] var5;
            if (obj == null) {
                var5 = equal;
                var4 = equal.length;

                for (var3 = 0; var3 < var4; ++var3) {
                    compare = var5[var3];
                    if (compare == null) {
                        return true;
                    }
                }
            } else {
                var5 = equal;
                var4 = equal.length;

                for (var3 = 0; var3 < var4; ++var3) {
                    compare = var5[var3];
                    if (obj.equals(compare)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    public static <T> int find(T obj, T[] array) {
        int i;
        if (obj == null) {
            for (i = 0; i < array.length; ++i) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else {
            for (i = 0; i < array.length; ++i) {
                if (obj.equals(array[i])) {
                    return i;
                }
            }
        }

        return -1;
    }

    private static void swap(List list, int i, int j) {
        Object tmp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, tmp);
    }

    private static void swap(Object[] arr, int i, int j) {
        Object tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    private static final SecureRandom rnd = new SecureRandom();

    public static <T> List<T> shuffle(List<T> list) {
        int size = list.size();

        for (int i = size; i > 1; i--)
            swap(list, i - 1, rnd.nextInt(i));

        return list;
    }

    public static <T> T[] shuffle(T... arr) {
        int size = arr.length;

        // Shuffle array
        for (int i = size; i > 1; i--)
            swap(arr, i - 1, rnd.nextInt(i));

        return arr;
    }

    public static String reverse(String s) {
        StringBuilder b = new StringBuilder();
        b.append(s);
        b.reverse();
        return b.toString();
    }

    public static byte[] toByteArray(String s) {
        try {
            return IOUtils.toByteArray(new InputStringStream(s));
        } catch (IOException ioE) {
            throw new RuntimeException("i/o exception while reading string", ioE);
        }
    }

    public static boolean equal(byte[] b1, byte[] b2) {
        if (b1 == b2) {
            return true;
        }

        if (b1.length == b2.length) {
            for (int i = 0; i < b1.length; i++) {
                if (b1[i] != b2[i])
                    return false;
            }
            return true;
        }

        return false;
    }

    public static <T> T requireNotNull(T obj, String name) {
        if (obj == null) {
            throw new NullPointerException(name);
        }
        return obj;
    }

    public static <T> T requireNotNull(T obj) {
        return requireNotNull(obj, null);
    }

    public static <T extends Collection> T requireNotContainNull(T collection, String name) {
        for (Object o : U.requireNotNull(collection, name)) {
            if (o == null) {
                throw new NullPointerException(name);
            }
        }
        return collection;
    }

    public static <T> T[] requireNotContainNull(T[] array, String name) {
        for (Object o : U.requireNotNull(array, name)) {
            if (o == null) {
                throw new NullPointerException(name);
            }
        }
        return array;
    }

    public static Locale getLocale(String tag) {
        if(tag == null) {
            return null;
        }

        for(Locale locale : Locale.getAvailableLocales()) {
            if(tag.equalsIgnoreCase(locale.toString())) {
                return locale;
            }
        }

        return null;
    }

    public static <T> List<T> asListOf(Class<T> of, Object... objects) {
        List<T> list = new ArrayList<T>(objects.length);
        for(Object o : objects) {
            list.add((T) o);
        }
        return list;
    }

    public static Calendar getUTC() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    }

    public static Calendar getUTC(long millis) {
        Calendar date = getUTC();
        date.setTimeInMillis(millis);
        return date;
    }

    public static String resolveHost(URL url) {
        if(url == null) {
            return "url is null";
        } else {
            return resolveHost(url.getHost());
        }
    }

    public static String resolveHost(String host) {
        String ip;
        if(host == null) {
            ip = "host is null";
        } else {
            try {
                ip = InetAddress.getByName(host).getHostAddress();
            } catch (UnknownHostException unknownHostException) {
                ip = "unknown host: " + host;
            }
        }
        return ip;
    }

    public static String readErrorResponse(HttpURLConnection connection) {
        if(connection == null) {
            return "connection is null";
        }
        if(connection.getErrorStream() == null) {
            return "no error stream";
        }
        try(InputStreamReader reader = new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8)) {
            return IOUtils.toString(reader);
        } catch (IOException e) {
            return "failed to read error stream: " + e.toString();
        }
    }

    public static String getNormalVersion(Version version) {
        return String.format(java.util.Locale.ROOT,
                "%d.%d.%d",
                version.getMajorVersion(),
                version.getMinorVersion(),
                version.getPatchVersion()
        );
    }

    public static String getMinorVersion(Version version) {
        return String.format(java.util.Locale.ROOT,
                "%s%d.%d",
                version.getMajorVersion() == 1 ? "" : version.getMajorVersion() + ".",
                version.getMinorVersion(),
                version.getPatchVersion()
        );
    }

    public static void copyInterruptibly(InputStream input, OutputStream output) throws IOException {
        byte[] b = new byte[8192];
        int l;
        while((l = input.read(b)) != -1) {
            output.write(b, 0, l);
            if(Thread.interrupted()) {
                throw new InterruptedIOException();
            }
        }
        output.flush();
    }

    public static void checkInterrupted() throws InterruptedException {
        if(Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    private static final Gson gson = new GsonBuilder().create();

    public static Gson getGson() {
        return gson;
    }
}
