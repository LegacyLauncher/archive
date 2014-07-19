package ru.turikhay.tlauncher.ui.converter.dummy;

public class DummyLongConverter extends DummyConverter<Long> {

	@Override
	public Long fromDummyString(String from) throws RuntimeException {
		return Long.parseLong(from);
	}

	@Override
	public String toDummyValue(Long value) throws RuntimeException {
		return value.toString();
	}

	@Override
	public Class<Long> getObjectClass() {
		return Long.class;
	}

}
