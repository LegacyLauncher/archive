package ru.turikhay.util;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class JavaVersionDetectorTest {

    @Test
    @Disabled
    void realTest() throws JavaVersionNotDetectedException, InterruptedException {
        JavaVersionDetector detector = new JavaVersionDetector(OS.getJavaPath());
        System.out.println(detector.detect());
    }

}