package ru.turikhay.tlauncher.bootstrap.util.stream;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Test
public class BufferedOutputStringStreamTest extends Assert {

    private final String checkLine = "привет\n";

    @Test
    public void test() throws Exception {
        BufferedOutputStringStream stream = new BufferedOutputStringStream() {
            public synchronized void flush() {
                assertEquals(bufferedLine, checkLine);
                super.flush();
            }
        };

        InputStream in = new ByteArrayInputStream(checkLine.getBytes());
        int read;
        while((read = in.read()) != -1) {
            stream.write(read);
        }
    }
}