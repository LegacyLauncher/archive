package net.legacylauncher.bootstrap.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Sha256Sign {
    public static String toString(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String s = Integer.toString(((int) b) & 0xFF, 16);
            for (int i = 2 - s.length(); i > 0; i--) {
                builder.append('0');
            }
            builder.append(s);
        }
        return builder.toString();
    }

    public static byte[] digest(InputStream in) throws IOException {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
        try {
            byte[] dataBytes = new byte[1024];
            int nread;
            while ((nread = in.read(dataBytes)) != -1) {
                digest.update(dataBytes, 0, nread);
            }
            return digest.digest();
        } finally {
            digest.reset();
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
