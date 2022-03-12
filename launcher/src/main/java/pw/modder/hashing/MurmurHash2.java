package pw.modder.hashing;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

public class MurmurHash2 {
    private static final long M32 = 0x5bd1e995;
    private static final int R32 = 24;
    private static final int SEED = 1;

    private MurmurHash2() {
    }

    public static long hash32(File inputFile) throws IOException {
        return hash32(inputFile, false);
    }

    public static long hash32normalized(File inputFile) throws IOException {
        return hash32(inputFile, true);
    }

    private static long hash32(File file, boolean normalized) throws IOException {
        long length = Files.size(file.toPath());
        if (length < 0)
            throw new IOException("invalid file size: " + length);

        // TODO fix shitcode
        int lengthMatching;
        try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(file))) {
            lengthMatching = countMatchingBytes(is, normalized);
        }

        try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(file))) {
            return hash32(is, lengthMatching, normalized);
        }
    }

    /*
        The following is ported from Apache Commons Codec
        https://github.com/apache/commons-codec/blob/3397682996c8dc618469b4ca910dd130540451e3/src/main/java/org/apache/commons/codec/digest/MurmurHash2.java
    */
    private static long hash32(BufferedInputStream inputStream, int length, boolean normalized) throws IOException {
        int hash = SEED ^ length;
        int k;
        int[] data = new int[4];
        int read;
        while ((read = readNextBytes(data, inputStream, normalized)) == 4) {
            k = data[0] | (data[1] << 8) | (data[2] << 16) | (data[3] << 24);
            k *= M32;
            k ^= k >>> R32;
            k *= M32;

            hash *= M32;
            hash ^= k;
        }

        switch (read) {
            case 3:
                hash ^= data[2] << 16;
            case 2:
                hash ^= data[1] << 8;
            case 1:
                hash ^= data[0];
                hash *= M32;
        }
        hash ^= hash >>> 13;
        hash *= M32;
        hash ^= hash >>> 15;
        return Integer.toUnsignedLong(hash);
    }

    private static int readNextBytes(int[] out, final BufferedInputStream is, boolean normalized) throws IOException {
        int readBytes = 0;
        int next;
        while (readBytes < 4) {
            next = is.read();
            if (next == -1) return readBytes;
            if (normalized && (next == 9 || next == 10 || next == 13 || next == 32)) {
                continue;
            }
            out[readBytes] = next;
            readBytes++;
        }
        return readBytes;
    }

    private static int countMatchingBytes(final BufferedInputStream is, boolean normalized) throws IOException {
        if (!normalized) return is.available();
        int count = 0;
        int read;
        while (true) {
            read = is.read();
            if (read == -1) break;
            if (read != 9 && read != 10 && read != 13 && read != 32) count++;
        }
        return count;
    }
}