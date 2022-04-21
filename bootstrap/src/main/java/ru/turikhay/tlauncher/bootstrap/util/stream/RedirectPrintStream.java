package ru.turikhay.tlauncher.bootstrap.util.stream;

import java.io.PrintStream;

public class RedirectPrintStream extends PrintStream {
    private final RedirectOutputStream out;

    RedirectPrintStream(RedirectOutputStream out) {
        super(out);
        this.out = out;
    }

    public void disableRecording() {
        out.disableRecording();
    }
}
