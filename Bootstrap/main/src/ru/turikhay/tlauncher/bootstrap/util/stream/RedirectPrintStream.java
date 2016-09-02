package ru.turikhay.tlauncher.bootstrap.util.stream;

import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.PrintStream;

public final class RedirectPrintStream {
    private static final StringBuffer globalBuffer = new StringBuffer();

    public static StringBuffer getBuffer() {
        return globalBuffer;
    }

    public static Redirector newRedirectorFor(PrintStream redirectTo) {
        U.requireNotNull(redirectTo, "given print stream");
        return new Redirector(new RedirectorOutputStream(redirectTo));
    }

    public static class Redirector extends PrintStream {
        private Redirector(RedirectorOutputStream out) {
            super(out);
        }
    }

    private static class RedirectorOutputStream extends BufferedOutputStringStream {
        private final PrintStream redirectTo;

        RedirectorOutputStream(PrintStream redirectTo) {
            this.redirectTo = redirectTo;
        }

        public synchronized void flush() {
            RedirectPrintStream.globalBuffer.append(bufferedLine);
            redirectTo.print(bufferedLine);
            redirectTo.flush();

            super.flush();
        }
    }
}
