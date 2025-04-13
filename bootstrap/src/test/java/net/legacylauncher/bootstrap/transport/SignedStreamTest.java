package net.legacylauncher.bootstrap.transport;

import net.legacylauncher.bootstrap.util.Compressor;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;

class SignedStreamTest {

    @Test
    @Disabled
    void test() throws IOException {
        Compressor.init();
        try (
                SignedStream signed = new SignedStream(new FileInputStream("bootstrap.json.mgz.signed"));
                InputStream input = Compressor.uncompressMarked(signed, true);
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            IOUtils.copy(input, out);
            System.out.println(out);
            signed.validateSignature();
        }
    }
}
