package ru.turikhay.tlauncher.ui.logger;

import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.logger.LoggerBuffer;
import ru.turikhay.tlauncher.logger.LoggerInterface;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;

public class SwingLogger implements LoggerInterface, LocalizableComponent {
    private final LoggerFrame frame;

    public SwingLogger(Configuration config) {
        this.frame = new LoggerFrame(config);
    }

    public void drainFrom(LoggerBuffer buffer) {
        String content = buffer.drain();
        frame.append(content);
    }

    public void show() {
        frame.showFrame();
    }

    public void dispose() {
        frame.disposeFrame();
    }

    public void setFolderAction(Runnable action) {
        frame.setFolderAction(action);
    }

    public void setSaveAction(Runnable action) {
        frame.setSaveAction(action);
    }

    public void setKillAction(Runnable action) {
        frame.setKillAction(action);
    }

    @Override
    public void print(String message) {
        frame.append(message);
    }

    @Override
    public void updateLocale() {
        frame.updateLocale();
    }
}
