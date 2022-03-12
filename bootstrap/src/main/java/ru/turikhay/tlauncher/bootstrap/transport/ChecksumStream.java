package ru.turikhay.tlauncher.bootstrap.transport;

import ru.turikhay.tlauncher.bootstrap.util.Sha256Sign;
import ru.turikhay.tlauncher.bootstrap.util.WeakObjectPool;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Objects;

public class ChecksumStream extends FilterInputStream {
    private final WeakObjectPool.ObjectRef<MessageDigest> ref;
    private MessageDigest digest;
    private byte[] byteDigest;

    public ChecksumStream(InputStream stream) {
        super(stream);
        this.ref = Sha256Sign.getDigest();
        this.digest = Objects.requireNonNull(ref.get(), "calc ref");
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
        digest = null;
        ref.free();
        super.close();
    }
}
