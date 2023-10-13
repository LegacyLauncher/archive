package net.legacylauncher.ui.explorer;

import net.legacylauncher.ui.loc.Localizable;

public class MediaFileExplorer extends FilteredFileExplorer {

    MediaFileExplorer() {
        addExtesion(ImageFileExplorer.IMAGE_EXTENSIONS);
        addExtesion("mp4", "flv");
    }

    @Override
    protected String getDescription() {
        return Localizable.get("explorer.type.media");
    }

    public static MediaFileExplorer newExplorer() throws Exception {
        try {
            return new MediaFileExplorer();
        } catch (Throwable var1) {
            throw new Exception("couldn't create explorer", var1);
        }
    }
}
