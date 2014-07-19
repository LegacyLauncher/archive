package ru.turikhay.tlauncher.ui.converter.dummy;

public class DummyIntegerConverter extends DummyConverter<Integer> {

	@Override
	public Integer fromDummyString(String from) throws RuntimeException {
		return Integer.parseInt(from);
	}

	@Override
	public String toDummyValue(Integer value) throws RuntimeException {
		return value.toString();
	}

	@Override
	public Class<Integer> getObjectClass() {
		return Integer.class;
	}

}
