package ru.turikhay.util;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class JavaVersionTest {

    @Test
    public void testJava8() throws Exception {
        parseVersion("1.8.0", 1, 8, 0, 0, false, 1.8d);
        parseVersion("1.8.1", 1, 8, 1, 0, false, 1.8d);
        parseVersion("1.8.0_125", 1, 8, 0, 125, false, 1.8d);
        parseVersion("1.8.1_71-ea", 1, 8, 1, 71, true, 1.8d);
    }

    @Test
    public void testJava9() throws Exception {
        parseVersion("9", 1, 9, 0, 0, false, 1.9d);
        parseVersion("9.0.1", 1, 9, 0, 1, false, 1.9d);
        parseVersion("9.1.2", 1, 9, 1, 2, false, 1.9d);
        parseVersion("9.20.3-ea", 1, 9, 20, 3, true, 1.9d);
        parseVersion("9.20.3-ea+b255", 1, 9, 20, 3, true, 1.9d);
    }

    private void parseVersion(JavaVersion version, int epoch, int major, int minor, int update, boolean ea, double doubleVer) {
        assertEquals(version.getEpoch(), epoch, "epoch");
        assertEquals(version.getMajor(), major, "major");
        assertEquals(version.getMinor(), minor, "minor");
        assertEquals(version.getUpdate(), update, "update");
        assertEquals(version.getDouble(), doubleVer, "doubleVer");
    }

    private void parseVersion(String str, int epoch, int major, int minor, int update, boolean ea, double doubleVer) {
        JavaVersion version = JavaVersion.parse(str);
        parseVersion(version, epoch, major, minor, update, ea, doubleVer);
        assertEquals(version.getVersion(), str);
    }

}