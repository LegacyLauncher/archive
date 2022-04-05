package ru.turikhay.tlauncher.ui.converter.dummy;

public class DummyIntegerConverter extends DummyConverter<Integer> {
    public Integer fromDummyString(String from) throws RuntimeException {
        return Integer.valueOf(Integer.parseInt(from));
    }

    public String toDummyValue(Integer value) throws RuntimeException {
        return value.toString();
    }

    public Class<Integer> getObjectClass() {
        return Integer.class;
    }
}
