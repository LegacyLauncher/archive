package ru.turikhay.tlauncher.ui.block;

public interface Blockable {
    void block(Object reason);

    void unblock(Object reason);
}
