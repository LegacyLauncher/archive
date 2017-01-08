package ru.turikhay.tlauncher.ui.loc;

import ru.turikhay.tlauncher.ui.converter.StringConverter;

public abstract class LocalizableStringConverter<T> implements StringConverter<T> {
    private final String prefix;

    public LocalizableStringConverter(String prefix) {
        this.prefix = prefix;
    }

    public String toString(T from) {
        return Localizable.get(getPath(from));
    }

    String getPath(T from) {
        String prefix = getPrefix();
        if (prefix != null && !prefix.isEmpty()) {
            String path = toPath(from);
            return prefix + "." + path;
        } else {
            return toPath(from);
        }
    }

    String getPrefix() {
        return prefix;
    }

    protected abstract String toPath(T var1);
}
