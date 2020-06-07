package ru.turikhay.tlauncher.ui.converter;

public interface StringConverter<T> {
    T fromString(String var1);

    String toString(T var1);

    String toValue(T var1);

    Class<T> getObjectClass();
}
