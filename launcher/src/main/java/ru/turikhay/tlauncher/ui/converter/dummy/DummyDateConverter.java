package ru.turikhay.tlauncher.ui.converter.dummy;

import net.minecraft.launcher.versions.json.DateTypeAdapter;

import java.util.Date;

public class DummyDateConverter extends DummyConverter<Date> {
    private final DateTypeAdapter dateAdapter = new DateTypeAdapter(false);

    public Date fromDummyString(String from) throws RuntimeException {
        return dateAdapter.parse(from);
    }

    public String toDummyValue(Date value) throws RuntimeException {
        return dateAdapter.format(value);
    }

    public Class<Date> getObjectClass() {
        return Date.class;
    }
}
