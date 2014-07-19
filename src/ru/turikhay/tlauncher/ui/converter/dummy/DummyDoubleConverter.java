package ru.turikhay.tlauncher.ui.converter.dummy;

public class DummyDoubleConverter extends DummyConverter<Double> {

	@Override
	public Double fromDummyString(String from) throws RuntimeException {
		return Double.parseDouble(from);
	}

	@Override
	public String toDummyValue(Double value) throws RuntimeException {
		return value.toString();
	}

	@Override
	public Class<Double> getObjectClass() {
		return Double.class;
	}

}
