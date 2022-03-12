package ru.turikhay.tlauncher.ui.explorer;

import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.util.FileUtil;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.regex.Pattern;

public class ImageFileFilter extends FileFilter {
    public static final Pattern extensionPattern = Pattern.compile("^(?:jp(?:e|)g|png)$", Pattern.CASE_INSENSITIVE);

    public boolean accept(File f) {
        String extension = FileUtil.getExtension(f);
        return extension == null || extensionPattern.matcher(extension).matches();
    }

    public String getDescription() {
        return Localizable.get("explorer.type.image");
    }
}
