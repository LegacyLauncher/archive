package ru.turikhay.tlauncher.ui.converter.dummy;

public class DummyStringConverter extends DummyConverter<String> {

	@Override
	public String fromDummyString(String from) throws RuntimeException {
		return from;
	}

	@Override
	public String toDummyValue(String value) throws RuntimeException {
		return value;
	}

	@Override
	public Class<String> getObjectClass() {
		return String.class;
	}

}
