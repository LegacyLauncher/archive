package ru.turikhay.tlauncher.ui.converter.dummy;

public class DummyStringConverter extends DummyConverter<String> {
    public String fromDummyString(String from) throws RuntimeException {
        return from;
    }

    public String toDummyValue(String value) throws RuntimeException {
        return value;
    }

    public Class<String> getObjectClass() {
        return String.class;
    }
}
