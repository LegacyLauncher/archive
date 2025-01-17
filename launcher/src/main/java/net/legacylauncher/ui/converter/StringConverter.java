package net.legacylauncher.ui.converter;

public interface StringConverter<T> {
    T fromString(String from);

    String toString(T from);

    String toValue(T from);

    Class<T> getObjectClass();
}
