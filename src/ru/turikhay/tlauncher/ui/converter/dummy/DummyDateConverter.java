package ru.turikhay.tlauncher.ui.converter.dummy;

import java.util.Date;

import net.minecraft.launcher.versions.json.DateTypeAdapter;

public class DummyDateConverter extends DummyConverter<Date> {
	private final DateTypeAdapter dateAdapter;
	
	public DummyDateConverter() {
		this.dateAdapter = new DateTypeAdapter();
	}

	@Override
	public Date fromDummyString(String from) throws RuntimeException {
		return dateAdapter.toDate(from);
	}

	@Override
	public String toDummyValue(Date value) throws RuntimeException {
		return dateAdapter.toString(value);
	}
	
	@Override
	public Class<Date> getObjectClass() {
		return Date.class;
	}

}
