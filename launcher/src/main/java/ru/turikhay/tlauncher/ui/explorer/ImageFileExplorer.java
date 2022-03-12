package ru.turikhay.tlauncher.ui.explorer;

import ru.turikhay.tlauncher.ui.loc.Localizable;

public class ImageFileExplorer extends FilteredFileExplorer {
    static final String[] IMAGE_EXTENSIONS = {"png", "jpg", "jpeg"};

    ImageFileExplorer() {
        addExtesion(IMAGE_EXTENSIONS);
        setAccessory(new ImageFilePreview(this));
        setAcceptAllFileFilterUsed(false);
    }

    @Override
    protected String getDescription() {
        return Localizable.get("explorer.type.image");
    }

    public static ImageFileExplorer newExplorer() throws Exception {
        try {
            return new ImageFileExplorer();
        } catch (Throwable var1) {
            throw new Exception("couldn't create explorer", var1);
        }
    }
}
