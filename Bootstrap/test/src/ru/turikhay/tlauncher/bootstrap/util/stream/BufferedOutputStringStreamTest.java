package ru.turikhay.tlauncher.bootstrap.util.stream;

import org.testng.annotations.Test;

import java.io.InputStream;

@Test
public class BufferedOutputStringStreamTest {

    @Test
    public void test() throws Exception {
        BufferedOutputStringStream stream = new BufferedOutputStringStream() {
            public synchronized void flush() {
                System.out.println("flushed: \"" + bufferedLine + "\"");
                super.flush();
            }
        };

        InputStream in = getClass().getResourceAsStream("test.txt");
        int read;
        while((read = in.read()) != -1) {
            stream.write(read);
        }
    }
}