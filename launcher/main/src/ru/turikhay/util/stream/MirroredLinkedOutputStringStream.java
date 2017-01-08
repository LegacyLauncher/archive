package ru.turikhay.util.stream;

import java.io.IOException;
import java.io.OutputStream;

public class MirroredLinkedOutputStringStream extends LinkedOutputStringStream {
    private OutputStream mirror;

    public OutputStream getMirror() {
        return mirror;
    }

    public void setMirror(OutputStream stream) {
        mirror = stream;
    }

    public void write(int b) {
        super.write(b);

        if (mirror != null) {
            try {
                mirror.write(b);
            } catch (IOException var3) {
                throw new Error("Cannot log into the mirror!", var3);
            }
        }
    }

    public void flush() {
        super.flush();

        if (mirror != null) {
            try {
                mirror.flush();
            } catch (IOException var2) {
                throw new Error("Cannot flush the mirror!", var2);
            }
        }

    }
}
