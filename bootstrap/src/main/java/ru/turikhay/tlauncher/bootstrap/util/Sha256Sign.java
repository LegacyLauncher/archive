package ru.turikhay.tlauncher.bootstrap.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha256Sign {
    private static final WeakObjectPool<MessageDigest> SHA256Pool = new WeakObjectPool<>(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException nsaE) {
            throw new Error(nsaE);
        }
    });

    public static WeakObjectPool.ObjectRef<MessageDigest> getDigest() {
        return SHA256Pool.get();
    }

    public static String toString(byte[] b) {
        return String.format(java.util.Locale.ROOT, "%064x", new java.math.BigInteger(1, b));
    }

    public static byte[] digest(byte[] b) {
        try {
            return digest(new ByteArrayInputStream(b));
        } catch (IOException e) {
            throw new Error("unexpected ioE", e);
        }
    }

    public static byte[] digest(InputStream in) throws IOException {
        WeakObjectPool.ObjectRef<MessageDigest> digestRef = SHA256Pool.get();
        final MessageDigest digest = digestRef.get();
        try {
            byte[] dataBytes = new byte[1024];
            int nread;
            while ((nread = in.read(dataBytes)) != -1) {
                digest.update(dataBytes, 0, nread);
            }
            return digest.digest();
        } finally {
            digest.reset();
            digestRef.free();
        }
    }

    public static String calc(Path file) throws IOException {
        try (InputStream input = Files.newInputStream(file)) {
            return toString(digest(input));
        }
    }

    private Sha256Sign() {
    }
}
