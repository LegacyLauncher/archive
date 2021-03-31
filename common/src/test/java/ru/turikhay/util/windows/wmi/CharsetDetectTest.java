package ru.turikhay.util.windows.wmi;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.turikhay.util.CharsetDetect;

class CharsetDetectTest {
    @Test
    @Disabled
    void realTest() {
        System.out.println(CharsetDetect.detect());
    }
}