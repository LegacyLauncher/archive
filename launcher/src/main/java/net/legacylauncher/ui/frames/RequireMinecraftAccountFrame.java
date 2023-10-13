package net.legacylauncher.ui.frames;

import net.legacylauncher.ui.images.Images;

import javax.swing.*;

public class RequireMinecraftAccountFrame extends VActionFrame {
    public RequireMinecraftAccountFrame() {
        setTitlePath("support.require_minecraft_account.title");
        getHead().setIcon(Images.getIcon32("logo-microsoft"));
        getHead().setText("support.require_minecraft_account.title");
        getBodyText().setText("support.require_minecraft_account.body");

        setType(Type.UTILITY);
        setAlwaysOnTop(true);
        setResizable(false);
        pack();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }
}
