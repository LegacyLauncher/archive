package ru.turikhay.tlauncher.ui.explorer;

import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.util.FileUtil;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class ExtensionFileFilter extends FileFilter {
    private final String extension;
    private final boolean acceptNull;

    public ExtensionFileFilter(String extension, boolean acceptNullExtension) {
        if (extension == null) {
            throw new NullPointerException("Extension is NULL!");
        } else if (extension.isEmpty()) {
            throw new IllegalArgumentException("Extension is empty!");
        } else {
            this.extension = extension;
            acceptNull = acceptNullExtension;
        }
    }

    public ExtensionFileFilter(String extension) {
        this(extension, true);
    }

    public String getExtension() {
        return extension;
    }

    public boolean acceptsNull() {
        return acceptNull;
    }

    public boolean accept(File f) {
        String currentExtension = FileUtil.getExtension(f);
        return acceptNull && currentExtension == null || extension.equals(currentExtension);
    }

    public String getDescription() {
        return Localizable.get("explorer.extension.format", extension.toUpperCase(java.util.Locale.ROOT));
    }
}
