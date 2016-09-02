package ru.turikhay.tlauncher.bootstrap.util;

import joptsimple.ValueConverter;

import java.io.File;

public class FolderValueConverter implements ValueConverter<File> {
    @Override
    public File convert(String value) {
        File folder = new File(value);
        if (folder.isDirectory()) {
            throw new IllegalArgumentException("folder is not found: " + value);
        }
        return folder;
    }

    @Override
    public Class<File> valueType() {
        return File.class;
    }

    @Override
    public String valuePattern() {
        return "<folder location>";
    }
}

