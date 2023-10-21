package net.legacylauncher.bootstrap.transport;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChecksumStream extends FilterInputStream {
    private final MessageDigest digest;
    private byte[] byteDigest;

    public ChecksumStream(InputStream stream) {
        super(stream);
        try {
            this.digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    public byte[] digest() {
        return digest != null ? digest.digest() : byteDigest != null ? byteDigest.clone() : null;
    }

    @Override
    public int read() throws IOException {
        int read = in.read();
        if (read != -1) {
            digest.update((byte) read);
        }
        return read;
    }

    public int read(byte[] b) throws IOException {
        int readLen = in.read(b);
        if (readLen != -1) {
            digest.update(b, 0, readLen);
        }
        return readLen;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int readLen = in.read(b, off, len);
        if (readLen != -1) {
            digest.update(b, off, readLen);
        }
        return readLen;
    }

    @Override
    public void close() throws IOException {
        byteDigest = digest.digest();
        super.close();
    }
}
