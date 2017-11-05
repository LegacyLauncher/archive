package ru.turikhay.tlauncher.bootstrap.util;

import shaded.org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import shaded.org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import shaded.org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import shaded.org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;

public abstract class Compressor {
    public static final int MARKER_SIZE = 5;
    private static Map<String, Compressor> compressorByMarker = new TreeMap<String, Compressor>();

    public static void init() {
        new GzipCompressor();
        new Bzip2Compressor();
    }

    public static class GzipCompressor extends Compressor {
        private GzipCompressor() {
            super("gzip", "gzip:");
        }

        @Override
        OutputStream _compress(OutputStream out) throws IOException {
            return new GzipCompressorOutputStream(out);
        }

        @Override
        InputStream _uncompress(InputStream in) throws IOException {
            return new GzipCompressorInputStream(in);
        }
    }

    public static class Bzip2Compressor extends Compressor {
        private Bzip2Compressor() {
            super("bzip2", "bzip:");
        }

        @Override
        OutputStream _compress(OutputStream out) throws IOException {
            return new BZip2CompressorOutputStream(out);
        }

        @Override
        InputStream _uncompress(InputStream in) throws IOException {
            return new BZip2CompressorInputStream(in);
        }
    }

    public static InputStream uncompressMarked(InputStream in, boolean failIfNoMarker) throws IOException {
        byte[] markerBytes = new byte[MARKER_SIZE];

        lookingForMarker:
        {
            if (in.read(markerBytes) < MARKER_SIZE) {
                if (failIfNoMarker) {
                    throw new MarkerNotFoundException();
                }
                break lookingForMarker;
            }

            String marker = new String(markerBytes);
            Compressor compressor = compressorByMarker.get(marker);

            if (compressor == null) {
                if (failIfNoMarker) {
                    throw new UnknownMarkerException(marker);
                }
                break lookingForMarker;
            }

            return compressor.uncompress(in);
        }

        return new SequenceInputStream(new ByteArrayInputStream(markerBytes), in);
    }

    public static InputStream uncompressMarked(InputStream in) throws IOException {
        return uncompressMarked(in, false);
    }

    final String name;
    final byte[] marker;

    protected Compressor(String name, byte[] marker) {
        this.name = name;

        if (marker == null)
            throw new NullPointerException("marker");

        if (marker.length != 5)
            throw new IllegalArgumentException("marker is too big: " + marker.length);

        this.marker = marker;

        compressorByMarker.put(new String(marker), this);
    }

    protected Compressor(String name, String shortName) {
        this(name, shortName.getBytes(U.UTF8));
    }

    public final String getName() {
        return name;
    }

    public final byte[] getMarker() {
        return marker.clone();
    }

    abstract OutputStream _compress(OutputStream out) throws IOException;

    abstract InputStream _uncompress(InputStream in) throws IOException;

    public final OutputStream compress(OutputStream out) throws IOException {
        return new CompressedOutputStream(this, _compress(out));
    }

    public final InputStream uncompress(InputStream in) throws IOException {
        return new CompressedInputStream(this, _uncompress(in));
    }

    public static class CompressedInputStream extends FilterInputStream {
        private final Compressor compressor;

        protected CompressedInputStream(Compressor compressor, InputStream delegator) {
            super(delegator);

            this.compressor = U.requireNotNull(compressor, "compressor");
        }

        public final Compressor getCompressor() {
            return compressor;
        }
    }

    public static class CompressedOutputStream extends FilterOutputStream {
        private final Compressor compressor;

        protected CompressedOutputStream(Compressor compressor, OutputStream delegator) {
            super(delegator);

            this.compressor = U.requireNotNull(compressor, "compressor");
        }

        public final Compressor getCompressor() {
            return compressor;
        }
    }

    public static class MarkerNotFoundException extends IOException {
    }

    public static class UnknownMarkerException extends IOException {
        public UnknownMarkerException(String marker) {
            super(marker);
        }
    }
}
