package ru.turikhay.tlauncher.ui.background;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.handlers.ExceptionHandler;
import ru.turikhay.util.U;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Worker {
    private static final float STEP = 0.025f;

    private final ExecutorService service = Executors.newSingleThreadExecutor();

    private final CoverTask
            showBackgroundTask = new CoverTask(0.f),
            hideBackgroundTask = new CoverTask(1.f);

    private final BackgroundManager wrapper;

    Worker(BackgroundManager wrapper) {
        this.wrapper = wrapper;
    }

    void showBackground() {
        submit(showBackgroundTask);
    }

    void hideBackground() {
        submit(hideBackgroundTask);
    }

    void setBackground(IBackground background, String path) {
        submit(new SetBackgroundTask(background, path));
    }

    private void submit(Runnable runnable, boolean requireAsync) {
        if (requireAsync || TLauncher.getInstance().isReady()) {
            service.submit(runnable);
        } else {
            runnable.run();
        }
    }

    private void submit(Runnable runnable) {
        submit(runnable, false);
    }

    private class CoverTask implements Runnable {
        final float targetOpacity;

        CoverTask(float targetOpacity) {
            this.targetOpacity = targetOpacity;
        }

        @Override
        public void run() {
            if (!TLauncher.getInstance().isReady()) {
                wrapper.cover.setOpacity(targetOpacity);
                return;
            }

            if (targetOpacity == 0.0f) {
                U.sleepFor(1000);
            }

            float opacity = wrapper.cover.getOpacity(), step = opacity > targetOpacity ? -STEP : STEP, eps = Math.abs(step / 2.f);
            log("setting opacity:", opacity, targetOpacity, step);

            while (Math.abs(opacity - targetOpacity) > eps) {
                wrapper.cover.setOpacity(opacity += step);
                U.sleepFor(16);
            }

            log("opacity set to", targetOpacity);
        }
    }

    private class SetBackgroundTask implements Runnable {
        private final IBackground background;
        private final String path;

        private SetBackgroundTask(IBackground background, String path) {
            this.background = U.requireNotNull(background, "background");
            this.path = path;
        }

        @Override
        public void run() {
            hideBackgroundTask.run();

            loadBackground:
            {
                wrapper.setBackground(background);

                try {
                    background.loadBackground(path);
                } catch (OutOfMemoryError outOfMemoryError) {
                    ExceptionHandler.reduceMemory(outOfMemoryError);
                    break loadBackground;
                } catch (Exception e) {
                    log("Could not load background for", background, "; path:", path, e);
                    break loadBackground;
                }
            }

            showBackgroundTask.run();
        }
    }

    private void log(Object... o) {
        U.log("[Background][Worker]", o);
    }

}
