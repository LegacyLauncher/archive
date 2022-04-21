package ru.turikhay.tlauncher.downloader;

import java.io.IOException;

public class PartialDownloadException extends IOException {
    private final long skipped, read, length;

    PartialDownloadException(long skipped, long read, long length) {
        super("read " + skipped + " + " + read + "=" + (skipped + read) + " bytes out of " + length + " bytes");
        this.skipped = skipped;
        this.read = read;
        this.length = length;
    }

    public long getSkipped() {
        return skipped;
    }

    public long getRead() {
        return read;
    }

    public long getNextSkip() {
        return skipped + read;
    }

    public long getLength() {
        return length;
    }
}
