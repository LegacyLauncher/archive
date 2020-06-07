package ru.turikhay.tlauncher.ui.explorer;

import ru.turikhay.tlauncher.ui.loc.Localizable;

public class ImageFileExplorer extends FilteredFileExplorer {
    static final String[] IMAGE_EXTENSIONS = {"png", "jpg", "jpeg"};

    ImageFileExplorer() {
        addExtesion((String[]) IMAGE_EXTENSIONS);
        setAccessory(new ImageFilePreview(this));
        setAcceptAllFileFilterUsed(false);
    }

    @Override
    protected String getDescription() {
        return Localizable.get("explorer.type.image");
    }

    public static ImageFileExplorer newExplorer() throws Exception {
        return new ImageFileExplorer();
    }
}
