package ru.turikhay.tlauncher.bootstrap.util;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Proxy;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class U {
    public static final Charset UTF8 = Charset.forName("UTF-8");
    private static final Random rnd = new Random();

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
        return Sha256Sign.calc(file);
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

    public static byte[] intToByte(int i) {
        byte[] b = new byte[4];
        b[3] = (byte) i;
        b[2] = (byte) (i >>> 8);
        b[1] = (byte) (i >>> 16);
        b[0] = (byte) (i >>> 24);
        return b;
    }

    public static int byteToInt(byte[] b) {
        int i = 0;
        switch(b.length) {
            case 4:
                i |= (b[3] & 0xFF);
            case 3:
                i |= (b[2] & 0xFF) << 8;
            case 2:
                i |= (b[1] & 0xFF) << 16;
            case 1:
                i |= b[0] << 24;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return i;
    }

    public static InputStreamReader toReader(InputStream in) {
        return new InputStreamReader(in, UTF8);
    }

    private static boolean nioAvailable = true;

    public static long queryFreeSpace(File file) {
        if(!file.exists()) {
            File parent = file.getParentFile();
            if(parent == null) {
                U.log("[queryFreeSpace]", "File", file.getAbsolutePath(),
                        "does not exist and does not have a parent.");
                // but it's *probably* okay. If not, we'll see this message in the bug report :)
            }
            file = parent;
        }
        if(nioAvailable) {
            try {
                return queryFreeSpaceNIO(file);
            } catch (Throwable t) {
                U.log("[NIO]", "Failed to query free space using NIO", t.toString());
                nioAvailable = false;
            }
        }
        return queryFreeSpaceOldIO(file);
    }

    private static long queryFreeSpaceNIO(File file) {
        Path path = file.toPath(); // -> NoSuchMethodError
        FileSystem fileSystem = path.getFileSystem();
        if(fileSystem.isReadOnly()) {
            U.log("[NIO]", "Filesystem is read-only", file);
            return 0;
        }
        FileStore fileStore;
        try {
            fileStore = fileSystem.provider().getFileStore(path);
        } catch (IOException e) {
            U.log("[NIO]", "Couldn't get file store of", file, e);
            return -1;
        }
        if(fileStore.isReadOnly()) {
            U.log("[NIO]", "File store is read-only", fileStore);
            return 0;
        }
        try {
            return fileStore.getUsableSpace();
        } catch (IOException e) {
            U.log("[NIO]", "Can't query usable space on", fileStore, e);
            return -1;
        }
    }

    private static long queryFreeSpaceOldIO(File file) {
        long freeSpace = file.getFreeSpace();
        return freeSpace <= 0 ? -1 : freeSpace;
    }

    private U() {
    }
}
