package ru.turikhay.tlauncher.ui.background;

public interface IBackground {
    void startBackground();

    void pauseBackground();

    // Must be called from WorkerThread
    void loadBackground(String path) throws Exception;
}
