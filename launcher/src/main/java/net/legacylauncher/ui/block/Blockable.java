package net.legacylauncher.ui.block;

public interface Blockable {
    void block(Object reason);

    void unblock(Object reason);
}
