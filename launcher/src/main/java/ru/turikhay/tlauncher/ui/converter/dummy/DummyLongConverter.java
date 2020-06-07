package ru.turikhay.tlauncher.ui.converter.dummy;

public class DummyLongConverter extends DummyConverter<Long> {
    public Long fromDummyString(String from) throws RuntimeException {
        return Long.valueOf(Long.parseLong(from));
    }

    public String toDummyValue(Long value) throws RuntimeException {
        return value.toString();
    }

    public Class<Long> getObjectClass() {
        return Long.class;
    }
}
