package ru.turikhay.util.async;


import javafx.application.Platform;
import ru.turikhay.tlauncher.handlers.ExceptionHandler;

public abstract class FxRunnable implements Runnable {

    final Runnable fxBridge = new Runnable() {
        @Override
        public void run() {
            try {
                runFx();
            } catch (Throwable t) {
                ExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), t);
            }
        }
    };

    @Override
    public void run() {
        if (Platform.isFxApplicationThread()) {
            fxBridge.run();
        } else {
            Platform.runLater(fxBridge);
        }
    }

    public abstract void runFx();
}
