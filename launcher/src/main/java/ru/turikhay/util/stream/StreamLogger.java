package ru.turikhay.util.stream;

public interface StreamLogger {
    void log(String s);

    void log(Object... o);

    void rawlog(String s);
}
