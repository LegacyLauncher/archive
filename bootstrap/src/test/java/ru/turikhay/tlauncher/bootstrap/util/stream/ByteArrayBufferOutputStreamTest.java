package ru.turikhay.tlauncher.bootstrap.util.stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ByteArrayBufferOutputStreamTest {
    private static final int COUNT = 16;

    ByteArrayBufferOutputStream stream;

    @BeforeEach
    public void setUp() {
        stream = new ByteArrayBufferOutputStream(COUNT);
    }

    @Test
    public void testSimpleWrites() throws IOException {
        stream.write('\u0046');
        stream.write(new byte[]{111, 111});
        stream.write(new byte[]{0, 33, 0}, 1, 1);
        assertEquals(stream.buffer().toString("UTF-8"), "Foo!");
    }

    @Test
    public void testClearBuffer() {
        stream.write(65);
        assertEquals(size(), 1);

        ByteArrayOutputStream oldBuffer = stream.buffer();

        stream.clearBuffer();
        assertEquals(size(), 0);
        assertNotEquals(stream.buffer(), oldBuffer);
    }

    @Test
    public void testOverflowWriteByte() throws IOException {
        write(COUNT);
        assertEquals(size(), COUNT);
        stream.write('a');
        assertEquals(size(), COUNT);
    }

    @Test
    public void testOverflowWriteArray() throws IOException {
        write(COUNT);
        assertEquals(size(), COUNT);
        stream.write(new byte[]{32, 32, 32, 32});
        assertEquals(size(), COUNT);
    }

    @Test
    public void testOverflowWriteBuffer() throws IOException {
        write(COUNT);
        assertEquals(size(), COUNT);
        stream.write(new byte[]{32, 32, 32, 32}, 2, 2);
        assertEquals(size(), COUNT);
    }

    private void write(int count) throws IOException {
        byte[] b = new byte[count];
        stream.write(b);
    }

    private int size() {
        return stream.buffer().size();
    }

}