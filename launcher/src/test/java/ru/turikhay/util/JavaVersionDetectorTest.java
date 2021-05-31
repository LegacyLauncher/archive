package ru.turikhay.util;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeoutException;

class JavaVersionDetectorTest {

    @Test
    @Disabled
    void realTest() throws JavaVersionNotDetectedException, InterruptedException, TimeoutException {
        JavaVersionDetector detector = new JavaVersionDetector(OS.getJavaPath());
        System.out.println(detector.detect());
    }

}