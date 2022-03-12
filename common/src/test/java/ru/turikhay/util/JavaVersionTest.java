package ru.turikhay.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaVersionTest {
    @Test
    public void test() {
        test("1.6.0", 1, 6, 0, 0, null, false);
        test("1.7.0-ea", 1, 7, 0, -1, "ea", true);
        test("1.8.0_60-b01", 1, 8, 0, 60, "b01", false);
        test("9", 1, 9, 0, 0, null, false);
        test("9.0.4", 1, 9, 0, 4, null, false);
        test("10", 1, 10, 0, 0, null, false);
        test("10.0.1", 1, 10, 0, 1, null, false);
        test("11.5", 1, 11, 5, 0, null, false);
        test("121.0.55", 1, 121, 0, 55, null, false);
        test("11.0.1", 1, 11, 0, 1, null, false);
        test("11.0.9.1", 1, 11, 0, 9, null, false);
        test("1.8.0-u252", 1, 8, 0, 252, null, false);
        test("16.0.1.9.1", 1, 16, 0, 1, null, false);
        test("16.0.1.9.1_3", 1, 16, 0, 1, null, false);
        test("1.8.0_232b09", 1, 8, 0, 232, null, false);
        test("1.8.0-262", 1, 8, 0, 262, null, false);
    }

    private void test(String parse, int epoch, int major, int minor, int update, String identifier, boolean ea) {
        JavaVersion javaVersion = JavaVersion.parse(parse);
        assertEquals(javaVersion.getEpoch(), epoch, "epoch");
        assertEquals(javaVersion.getMajor(), major, "major");
        assertEquals(javaVersion.getMinor(), minor, "minor");
        assertEquals(javaVersion.getUpdate(), update, "update");
        assertEquals(javaVersion.getIdentifier(), identifier, "identifier");
        assertEquals(javaVersion.isEarlyAccess(), ea, "earlyAccess");
    }
}