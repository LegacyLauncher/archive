package ru.turikhay.tlauncher.bootstrap.util.stream;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static ru.turikhay.tlauncher.bootstrap.util.stream.BufferUtils.*;

public class RedirectPrintStreamTest {
    private ByteArrayOutputStream sysOutBuffer;
    private PrintStream sysOut;

    private ByteArrayOutputStream redirBuffer;
    private RedirectOutputStream redirOutStream;
    private RedirectPrintStream redirPrintStream;

    @BeforeEach
    public void setUp() {
        sysOutBuffer = new ByteArrayOutputStream();
        sysOut = new PrintStream(sysOutBuffer);

        redirBuffer = new ByteArrayOutputStream();
        redirOutStream = new RedirectOutputStream(redirBuffer, sysOut);
        redirPrintStream = new RedirectPrintStream(redirOutStream);
    }

    @Test
    public void testPrintln() {
        String input = "hello";
        String expected = "hello" + NEW_LINE;

        redirPrintStream.println(input);

        assertEquals(bufferToString(redirBuffer), expected);
        assertEquals(bufferToString(sysOutBuffer), expected);
    }

    @Test
    public void testNoRecording() {
        String input = "hello";
        String
                expectedSysOut = "hello" + NEW_LINE,
                expectedRedir = "";

        redirPrintStream.disableRecording();
        redirPrintStream.println(input);

        assertEquals(bufferToString(redirBuffer), expectedRedir);
        assertEquals(bufferToString(sysOutBuffer), expectedSysOut);
    }
}