package ru.turikhay.tlauncher.bootstrap.transport;

import org.testng.TestException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;
import ru.turikhay.tlauncher.bootstrap.util.U;
import shaded.com.getsentry.raven.util.Base64;
import shaded.org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.testng.Assert.*;

public class SignedStreamTest {
    // "Hello, World!" signed with private key
    static final byte[] _signedChecksum = Base64.decode("eHLWDAJL4LRbMod8fEVyiN46PMktN9vlHZWYpDybeNyxkDi8FnXLAVRl1pyOpyRMDj/wug8KI4wpj9ngkMD2rNTs7Ib5ulFfyKKi0vKIZm0+j9XSu79BKJ8zu5Kavl34VXnAVXyuWrhvLwH4F1O2lbDycRgFUHJmk/As6vQWVgYdXNJNw87PziTDiOM0pLOAbOcy+5dWEjfR1ym68d6FTgGiM49qNZtz/at4MnWCBFQicckS7WZVQDTnMHZFApMntG2H0TdcEWairyx5QL1m2XDsX/+P4ZVFs0lL5nsnD91yNYby1pC/O+r1AJFQxE4B9ynoAG40lXMAbiMVXctlag==", Base64.DEFAULT);

    static final byte[] _signedDataWithChecksum;
    static {
        byte[] data = ChecksumStreamTest.data();
        byte[] lengthBuffer = U.intToByte(_signedChecksum.length);
        int pointer = 0;
        _signedDataWithChecksum = new byte[lengthBuffer.length + _signedChecksum.length + data.length];

        System.arraycopy(lengthBuffer, 0, _signedDataWithChecksum, pointer, lengthBuffer.length);
        pointer += lengthBuffer.length;

        System.arraycopy(_signedChecksum, 0, _signedDataWithChecksum, pointer, _signedChecksum.length);
        pointer += _signedChecksum.length;

        System.arraycopy(data, 0, _signedDataWithChecksum, pointer, data.length);
        pointer += data.length;
    }

    static {
        byte[] checksum = ChecksumStreamTest.checksum();
        assertTrue(SignedStream.verify(checksum, _signedChecksum));
    }

    static byte[] signedChecksum() {
        return _signedChecksum.clone();
    }

    static byte[] signedDataWithChecksum() {
        return _signedDataWithChecksum.clone();
    }

    private SignedStream stream() {
        return new SignedStream(new ByteArrayInputStream(signedData));
    }

    private byte[] signedChecksum, signedData, checksum, data;

    @BeforeMethod
    public void setUp() throws Exception {
        signedChecksum = signedChecksum();
        signedData = signedDataWithChecksum();
        checksum = ChecksumStreamTest.checksum();
        data = ChecksumStreamTest.data();
    }

    @Test
    public void testValidateWithSignature() throws Exception {
        SignedStream s = stream();
        byte[] b = IOUtils.toByteArray(s);
        s.close();
        s.validateSignature();
    }

    @Test
    public void testRead() throws Exception {
        SignedStream s = stream();
        for(byte b : data) {
            int read = s.read();
            assertEquals(read, b);
        }
        expectEnd(s);
    }

    @Test
    public void testRead1() throws Exception {
        SignedStream s = stream();
        byte[] buffer = new byte[3];
        int remaining = data.length;
        int read;
        do {
            read = s.read(buffer);
            if(read == -1) {
                assertEquals(remaining, 0, "read -1 earlier than expected");
                break;
            }
            remaining -= read;
        } while(remaining > 0);
        expectEnd(s);
    }

    @Test
    public void testRead2() throws Exception {
        SignedStream s = stream();
        byte[] buffer = new byte[5];
        int remaining = data.length;
        int read;
        do {
            read = s.read(buffer, 0, 3);
            if(read == -1) {
                assertEquals(remaining, 0, "read -1 earlier than expected");
                break;
            }
            remaining -= read;
        } while(remaining > 0);
        expectEnd(s);
    }

    @Test
    public void testReadSignature() throws Exception {
        testBadSignatureLength(Integer.MIN_VALUE, true);
        testBadSignatureLength(-1, true);
        testBadSignatureLength(0, true);
        testBadSignatureLength(63, true);
        testBadSignatureLength(64, false);
        testBadSignatureLength(signedChecksum.length, false); // back to default

        SignedStream stream = stream();
        assertTrue(stream.readSignature(), "signature already read?");
        ArrayAsserts.assertArrayEquals(signedChecksum, stream.signedChecksum);
    }

    private void testBadSignatureLength(int i, boolean expectException) throws Exception {
        writeInt(signedData, i, 0);
        int read;
        try {
            read = stream().read();
        } catch(StreamNotSignedException exception) {
            if(expectException) {
                return;
            }
            throw exception;
        }
        if(expectException) {
            throw new TestException("expected exception is not thrown; read: " + read);
        }
    }

    private static void writeInt(byte[] data, int i, int pos) {
        System.arraycopy(U.intToByte(i), 0, data, pos, 4);
    }

    private static void expectEnd(InputStream s) throws Exception {
        assertEquals(s.read(), -1, "expected end of stream");
    }
}