package ru.turikhay.tlauncher.bootstrap.util;

import java.io.*;
import java.net.Proxy;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

public final class U {
    public static final Charset UTF8 = Charset.forName("UTF-8");
    private static final MessageDigest SHA256;

    static {
        try {
            SHA256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException nsaE) {
            throw new Error(nsaE);
        }
    }

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
                        System.out.print(toString((Throwable) anO));
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
        FileInputStream fis = new FileInputStream(file);

        byte[] dataBytes = new byte[1024];
        int nread;
        while ((nread = fis.read(dataBytes)) != -1) {
            SHA256.update(dataBytes, 0, nread);
        }

        byte[] mdbytes = SHA256.digest();

        StringBuilder hexString = new StringBuilder();

        for (byte mdbyte : mdbytes) {
            hexString.append(Integer.toHexString(0xFF & mdbyte));
        }

        return hexString.toString();
    }

    public static File getJar() {
        try {
            return new File(URLDecoder.decode(U.class.getProtectionDomain().getCodeSource().getLocation().getPath(), UTF8.name()));
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
        for(Locale l : Locale.getAvailableLocales()) {
            if(l.toString().equals(locale)) {
                return l;
            }
        }
        return null;
    }

    private U() {
    }
}
