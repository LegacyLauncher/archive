package ru.turikhay.util.windows;

import org.testng.annotations.Test;
import ru.turikhay.util.U;
import ru.turikhay.util.windows.wmi.WMI;

public class WMITest {
    @Test
    public void test() throws Exception {
        System.out.println(WMI.getAVSoftwareList());
    }
}