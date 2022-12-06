package ru.turikhay.tlauncher.ui.converter.dummy;

import ru.turikhay.tlauncher.ui.converter.StringConverter;

public abstract class DummyConverter<T> implements StringConverter<T> {

    public T fromString(String from) {
        return fromDummyString(from);
    }

    public String toString(T from) {
        return toValue(from);
    }

    public String toValue(T from) {
        return toDummyValue(from);
    }

    public abstract T fromDummyString(String var1) throws RuntimeException;

    public abstract String toDummyValue(T var1) throws RuntimeException;
}
