package ru.turikhay.tlauncher.bootstrap.util;

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class U {
    public static final Charset UTF8 = Charset.forName("UTF-8");
    private static final Random rnd = new Random();
    private static final WeakObjectPool<MessageDigest> SHA256Pool = new WeakObjectPool<MessageDigest>(new Factory<MessageDigest>() {
        @Override
        public MessageDigest createNew() {
            try {
                return MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException nsaE) {
                throw new Error(nsaE);
            }
        }
    });

    public static void log(String prefix, Object... o) {
        synchronized (System.out) {
            System.out.print(prefix);
            System.out.print(' ');
            if (o == null) {
                System.out.println("null");
            } else {
                for (Object anO : o) {
                    if (anO instanceof String) {
                        System.out.print(anO);
                    } else if (anO instanceof Throwable) {
                        System.out.println();
                        System.out.println(toString((Throwable) anO));
                        //System.err.println(toString((Throwable) anO));
                    } else {
                        System.out.print(" ");
                        System.out.print(anO);
                    }
                }
                System.out.println();
            }
        }
    }

    public static <T> T requireNotNull(T obj, String name) {
        if (obj == null) {
            throw new NullPointerException(name);
        }
        return obj;
    }

    public static Proxy getProxy() {
        return Proxy.NO_PROXY;
    }

    public static void close(Closeable... cl) {
        for (Closeable c : cl) {
            try {
                c.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public static String getSHA256(File file) throws IOException {
        WeakObjectPool<MessageDigest>.ObjectRef<MessageDigest> digestRef = SHA256Pool.get();
        try {
            final MessageDigest digest = digestRef.get();
            FileInputStream fis = new FileInputStream(file);
            byte[] dataBytes = new byte[1024];
            int nread;

            while ((nread = fis.read(dataBytes)) != -1) {
                digest.update(dataBytes, 0, nread);
            }

            return String.format("%064x", new java.math.BigInteger(1, digest.digest()));
        } finally {
            digestRef.free();
        }
    }

    public static File getJar(Class clazz) {
        try {
            return new File(URLDecoder.decode(clazz.getProtectionDomain().getCodeSource().getLocation().getPath(), UTF8.name()));
        } catch (UnsupportedEncodingException ueE) {
            throw new Error(ueE);
        }
    }

    public static String toString(Throwable t) {
        if (t == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(out));
        return new String(out.toByteArray());
    }

    public static Locale getLocale(String locale) {
        if (locale == null) {
            return null;
        }
        for (Locale l : Locale.getAvailableLocales()) {
            if (l.toString().equals(locale)) {
                return l;
            }
        }
        return null;
    }

    private static <T> void swap(T[] arr, int i, int j) {
        T tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    public static <T> T[] shuffle(T... arr) {
        // Shuffle array
        for (int i = arr.length; i > 1; i--)
            swap(arr, i - 1, rnd.nextInt(i));
        return arr;
    }

    public static URL toUrl(String url) {
        try {
            return new URL(url);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static String urlEncode(String text) {
        try {
            return URLEncoder.encode(text, UTF8.name());
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    public static void createDir(File dir) throws IOException {
        if (!dir.mkdirs() && !dir.isDirectory()) {
            throw new IOException("could not create dir: " + dir.getAbsolutePath());
        }
    }

    public static void createFile(File file) throws IOException {
        if(file.isFile()) {
            return;
        }

        IOException cause = null;
        createFile:
        {
            if (file.getParentFile() != null) {
                try {
                    createDir(file.getParentFile());
                } catch (IOException ioE) {
                    cause = ioE;
                    break createFile;
                }
            }

            try {
                file.createNewFile();
            } catch (IOException ioE) {
                cause = ioE;
                break createFile;
            }

            if (file.isFile()) {
                return;
            }
        }

        throw new IOException("could not create file: " + file.getAbsolutePath(), cause);
    }

    public static <T> T[] toArray(List<T> list, Class<T> classOfT) {
        T[] arr = (T[]) Array.newInstance(classOfT, list.size());
        list.toArray(arr);
        return arr;
    }

    public static InputStreamReader toReader(InputStream in) {
        return new InputStreamReader(in, UTF8);
    }

    private U() {
    }
}
