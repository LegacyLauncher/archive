package ru.turikhay.util.stream;

import ru.turikhay.util.U;

import java.io.PrintStream;
import java.util.Formatter;
import java.util.Locale;

public class PrintLogger extends PrintStream implements StreamLogger {
    private LinkedOutputStringStream stream;
    private StreamLogger mirror;
    private Formatter formatter;

    private PrintLogger(LinkedOutputStringStream stream, StreamLogger mirror) {
        super(stream);
        this.stream = stream;
        this.mirror = mirror;
    }

    public PrintLogger(LinkedOutputStringStream stream) {
        this(stream, null);
    }

    public StreamLogger getMirror() {
        return mirror;
    }

    public void setMirror(StreamLogger logger) {
        if (this == logger) {
            throw new IllegalArgumentException();
        } else {
            mirror = logger;
        }
    }

    public void log(Object... o) {
        log(U.toLog(o));
    }

    public void log(String s) {
        println(s);
        if (mirror != null) {
            mirror.log(s);
        }
    }

    public void rawlog(String s) {
        if (mirror != null) {
            mirror.rawlog(s);
        }
    }

    public LinkedOutputStringStream getStream() {
        return stream;
    }

    public synchronized void write(int b) {
        stream.write(b);
    }

    public synchronized void write(byte[] buf, int off, int len) {
        stream.write(buf, off, len);
    }

    private synchronized void write(String s) {
        if (s == null) {
            s = "null";
        }

        stream.write(s);
    }

    public synchronized void print(char c) {
        stream.write(c);
    }

    public synchronized void print(char[] s) {
        stream.write(new String(s));
    }

    public synchronized void print(boolean b) {
        write(b ? "true" : "false");
    }

    public synchronized void print(int i) {
        write(String.valueOf(i));
    }

    public synchronized void print(long l) {
        write(String.valueOf(l));
    }

    public synchronized void print(float f) {
        write(String.valueOf(f));
    }

    public synchronized void print(double d) {
        write(String.valueOf(d));
    }

    public synchronized void print(String s) {
        write(s);
    }

    public synchronized void print(Object obj) {
        write(String.valueOf(obj));
    }

    private void newLine() {
        write(10);
    }

    public synchronized void println() {
        newLine();
    }

    public synchronized void println(boolean x) {
        print(x);
        newLine();
    }

    public synchronized void println(char x) {
        print(x);
        newLine();
    }

    public synchronized void println(int x) {
        print(x);
        newLine();
    }

    public synchronized void println(long x) {
        print(x);
        newLine();
    }

    public synchronized void println(float x) {
        print(x);
        newLine();
    }

    public synchronized void println(double x) {
        print(x);
        newLine();
    }

    public synchronized void println(char[] x) {
        print(x);
        newLine();
    }

    public synchronized void println(String x) {
        print(x);
        newLine();
    }

    public synchronized void println(Object x) {
        print(String.valueOf(x));
        newLine();
    }

    public synchronized PrintStream printf(String format, Object... args) {
        return format(format, args);
    }

    public synchronized PrintStream printf(Locale l, String format, Object... args) {
        return format(l, format, args);
    }

    public synchronized PrintStream format(String format, Object... args) {
        if (formatter == null || formatter.locale() != Locale.getDefault()) {
            formatter = new Formatter(this);
        }

        formatter.format(Locale.getDefault(), format, args);
        return this;
    }

    public synchronized PrintStream format(Locale l, String format, Object... args) {
        if (formatter == null || formatter.locale() != l) {
            formatter = new Formatter(this, l);
        }

        formatter.format(l, format, args);
        return this;
    }

    public synchronized PrintStream append(CharSequence csq) {
        if (csq == null) {
            print("null");
        } else {
            print(csq.toString());
        }

        return this;
    }

    public synchronized PrintStream append(CharSequence csq, int start, int end) {
        Object cs = csq == null ? "null" : csq;
        write(((CharSequence) cs).subSequence(start, end).toString());
        return this;
    }

    public synchronized PrintStream append(char c) {
        print(c);
        return this;
    }
}
