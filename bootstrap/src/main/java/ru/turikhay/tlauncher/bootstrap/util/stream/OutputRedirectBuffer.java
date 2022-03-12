package ru.turikhay.tlauncher.bootstrap.util.stream;

import java.io.PrintStream;

public final class OutputRedirectBuffer {
    private static final ToStringBufferOutputStream buffer = new ByteArrayBufferOutputStream();

    public static String getBuffer() {
        return buffer.getBuffer();
    }

    public static void clearBuffer() {
        buffer.clearBuffer();
    }

    public static RedirectPrintStream createRedirect(PrintStream redirectTo) {
        return new RedirectPrintStream(new RedirectOutputStream(buffer, redirectTo));
    }

}
