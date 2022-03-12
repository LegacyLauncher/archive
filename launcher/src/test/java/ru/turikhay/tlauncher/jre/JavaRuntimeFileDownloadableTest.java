package ru.turikhay.tlauncher.jre;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.fluent.Request;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tukaani.xz.LZMAInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class JavaRuntimeFileDownloadableTest {

    @Test
    @Disabled
    void extractTest() throws IOException {
        File tempFile = File.createTempFile(getClass().getName(), null);
        tempFile.deleteOnExit();
        Request.Get("https://launcher.mojang.com/v1/objects/2130fec1b55591562f1fe8366875adbd37eb2107/" +
                "logging.properties").execute().saveContent(tempFile);
        try (LZMAInputStream input = new LZMAInputStream(new BufferedInputStream(new FileInputStream(tempFile)))
        ) {
            System.out.println(IOUtils.toString(input, StandardCharsets.UTF_8));
        }
    }
}