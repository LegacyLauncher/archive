package ru.turikhay.tlauncher.ui.converter.dummy;

public class DummyDoubleConverter extends DummyConverter<Double> {
    public Double fromDummyString(String from) throws RuntimeException {
        return Double.valueOf(Double.parseDouble(from));
    }

    public String toDummyValue(Double value) throws RuntimeException {
        return value.toString();
    }

    public Class<Double> getObjectClass() {
        return Double.class;
    }
}
