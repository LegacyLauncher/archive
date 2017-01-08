package ru.turikhay.tlauncher.bootstrap.util;

import shaded.joptsimple.ValueConverter;

import java.io.File;

public class FileValueConverter implements ValueConverter<File> {
    @Override
    public File convert(String value) {
        return new File(U.requireNotNull(value, "value"));
    }

    @Override
    public Class<File> valueType() {
        return File.class;
    }

    @Override
    public String valuePattern() {
        return "<file/folder location>";
    }
}

