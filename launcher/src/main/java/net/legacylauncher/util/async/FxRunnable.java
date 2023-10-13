package net.legacylauncher.util.async;


import javafx.application.Platform;
import net.legacylauncher.handlers.ExceptionHandler;

public abstract class FxRunnable implements Runnable {

    final Runnable fxBridge = () -> {
        try {
            runFx();
        } catch (Throwable t) {
            ExceptionHandler.getInstance().uncaughtException(Thread.currentThread(), t);
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
