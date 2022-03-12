package ru.turikhay.tlauncher.bootstrap.util.stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RedirectPrintStreamTest {
    private ByteArrayOutputStream sysOutBuffer;

    private ByteArrayOutputStream redirBuffer;
    private RedirectPrintStream redirPrintStream;

    @BeforeEach
    public void setUp() {
        sysOutBuffer = new ByteArrayOutputStream();
        PrintStream sysOut = new PrintStream(sysOutBuffer);

        redirBuffer = new ByteArrayOutputStream();
        RedirectOutputStream redirOutStream = new RedirectOutputStream(redirBuffer, sysOut);
        redirPrintStream = new RedirectPrintStream(redirOutStream);
    }

    @Test
    public void testPrintln() {
        String input = "hello";
        String expected = "hello" + System.lineSeparator();

        redirPrintStream.println(input);

        assertEquals(redirBuffer.toString(), expected);
        assertEquals(sysOutBuffer.toString(), expected);
    }

    @Test
    public void testNoRecording() {
        String input = "hello";
        String
                expectedSysOut = "hello" + System.lineSeparator(),
                expectedRedir = "";

        redirPrintStream.disableRecording();
        redirPrintStream.println(input);

        assertEquals(redirBuffer.toString(), expectedRedir);
        assertEquals(sysOutBuffer.toString(), expectedSysOut);
    }
}