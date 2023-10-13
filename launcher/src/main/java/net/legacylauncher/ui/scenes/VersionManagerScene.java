package net.legacylauncher.ui.scenes;

import net.legacylauncher.ui.MainPane;
import net.legacylauncher.ui.versions.VersionHandler;

public class VersionManagerScene extends PseudoScene {
    private static final long serialVersionUID = 758826812081732720L;
    final VersionHandler handler = new VersionHandler(this);

    public VersionManagerScene(MainPane main) {
        super(main);
        add(handler.list);
    }

    public void onResize() {
        super.onResize();
        handler.list.setLocation(getWidth() / 2 - handler.list.getWidth() / 2, getHeight() / 2 - handler.list.getHeight() / 2);
    }
}
