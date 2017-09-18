package ru.turikhay.tlauncher.bootstrap.transport;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;
import ru.turikhay.tlauncher.bootstrap.util.Sha256Sign;
import ru.turikhay.tlauncher.bootstrap.util.U;
import shaded.org.apache.commons.io.IOUtils;

import java.io.*;

import static org.testng.Assert.*;

public class ChecksumStreamTest {
    private static final byte[] _data;
    static {
        InputStream input = null;
        try {
            input = ChecksumStreamTest.class.getResourceAsStream("text.txt");
            _data = IOUtils.toByteArray(input);
        } catch(Exception e) {
            throw new Error(e);
        } finally {
            U.close(input);
        }
    }
    private static final byte[] _checksum = Sha256Sign.toString(Sha256Sign.digest(_data)).getBytes(U.UTF8);

    static byte[] data() {
        return _data.clone();
    }

    static byte[] checksum() {
        return _checksum.clone();
    }

    private byte[] data, checksum;

    @BeforeMethod
    public void setUp() throws Exception {
        data = data();
        checksum = checksum();
        U.log("Checksum:" + Sha256Sign.toString(checksum));
    }

    private ChecksumStream stream() {
        return new ChecksumStream(new ByteArrayInputStream(data));
    }

    @Test
    public void testDigest() throws Exception {
        ChecksumStream s = stream();
        byte[] b = new byte[data.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) s.read();
        }
        expectEnd(s);
        validateDigest(s);
    }

    @Test
    public void testDigestReadBuffer() throws Exception {
        ChecksumStream s = stream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[3];
        int read;
        while((read = s.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        expectEnd(s);
        validateDigest(s);
    }

    @Test
    public void testDigestReadBufferOffset() throws Exception {
        ChecksumStream s = stream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[5];
        int read;
        while((read = s.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, read);
        }
        expectEnd(s);
        validateDigest(s);
    }

    private void validateDigest(ChecksumStream s) {
        ArrayAsserts.assertArrayEquals(checksum, s.digest());
    }

    private static void expectEnd(InputStream s) throws Exception {
        assertEquals(s.read(), -1, "expected end of stream");
    }
}