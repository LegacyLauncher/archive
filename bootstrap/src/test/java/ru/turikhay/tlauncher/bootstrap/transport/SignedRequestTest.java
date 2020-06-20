package ru.turikhay.tlauncher.bootstrap.transport;

import org.testng.annotations.Test;
import ru.turikhay.tlauncher.bootstrap.util.U;
import org.apache.commons.io.IOUtils;

import java.net.URL;
import java.util.zip.GZIPInputStream;

public class SignedRequestTest {
    private final URL url;
    {
        try {
            url = new URL("http://localhost/output.bin");
        } catch(Exception e) {
            throw new Error(e);
        }
    }

    @Test
    public void makeRequest() throws Exception {
        GZIPInputStream s = new GZIPInputStream(new SignedStream(url.openStream()));
        String result = IOUtils.toString(s, U.UTF8);
        System.out.println(result);
        s.close();
    }
}
