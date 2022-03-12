package ru.turikhay.tlauncher.bootstrap.util;

import joptsimple.ValueConverter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class PathValueConverter implements ValueConverter<Path> {
    @Override
    public Path convert(String value) {
        return Paths.get(Objects.requireNonNull(value, "value"));
    }

    @Override
    public Class<Path> valueType() {
        return Path.class;
    }

    @Override
    public String valuePattern() {
        return "<file/folder location>";
    }
}

