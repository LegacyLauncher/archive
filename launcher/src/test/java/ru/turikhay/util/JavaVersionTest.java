package ru.turikhay.util;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class JavaVersionTest {

    @Test
    public void testJava8() throws Exception {
        parseVersion("1.8.0", 8, 0, 0, false);
        parseVersion("1.8.1", 8, 1, 0, false);
        parseVersion("1.8.0_125", 8, 0, 125, false);
        parseVersion("1.8.1_71-ea", 8, 1, 71, true);
    }

    @Test
    public void testJava9() throws Exception {
        parseVersion("9", 9, 0, 0, false);
        parseVersion("9.0.1", 9, 0, 1, false);
        parseVersion("9.1.2", 9, 1, 2, false);
        parseVersion("9.20.3-ea", 9, 20, 3, true);
        parseVersion("9.20.3-ea+b255", 9, 20, 3, true);
    }

    private void parseVersion(JavaVersion version, int major, int minor, int update, boolean ea) {
        assertEquals(version.getMajor(), major, "major");
        assertEquals(version.getMinor(), minor, "minor");
        assertEquals(version.getUpdate(), update, "update");
    }

    private void parseVersion(String str, int major, int minor, int update, boolean ea) {
        JavaVersion version = JavaVersion.parse(str);
        parseVersion(version, major, minor, update, ea);
        assertEquals(version.getVersion(), str);
    }

}