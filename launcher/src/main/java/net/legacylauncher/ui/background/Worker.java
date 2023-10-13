package net.legacylauncher.ui.background;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.handlers.ExceptionHandler;
import net.legacylauncher.util.U;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Worker {
    private static final Logger LOGGER = LogManager.getLogger(Worker.class);

    private static final float STEP = 0.025f;

    private final ExecutorService service = Executors.newSingleThreadExecutor();

    private final CoverTask
            showBackgroundTask = new CoverTask(0.f),
            hideBackgroundTask = new CoverTask(1.f);

    private final BackgroundManager wrapper;

    Worker(BackgroundManager wrapper) {
        this.wrapper = wrapper;
    }

    void setBackground(IBackground background, String path) {
        submit(new SetBackgroundTask(background, path));
    }

    private void submit(Runnable runnable) {
        if (LegacyLauncher.getInstance().isReady()) {
            service.submit(runnable);
        } else {
            runnable.run();
        }
    }

    private class CoverTask implements Runnable {
        final float targetOpacity;

        CoverTask(float targetOpacity) {
            this.targetOpacity = targetOpacity;
        }

        @Override
        public void run() {
            if (!LegacyLauncher.getInstance().isReady()) {
                wrapper.cover.setOpacity(targetOpacity);
                return;
            }

            if (targetOpacity == 0.0f) {
                U.sleepFor(1000);
            }

            float opacity = wrapper.cover.getOpacity(), step = opacity > targetOpacity ? -STEP : STEP, eps = Math.abs(step / 2.f);
            LOGGER.debug("setting opacity: {}, targetOpacity: {}, step: {}", opacity, targetOpacity, step);

            while (Math.abs(opacity - targetOpacity) > eps) {
                wrapper.cover.setOpacity(opacity += step);
                U.sleepFor(16);
            }

            LOGGER.debug("opacity set to {}", targetOpacity);
        }
    }

    private class SetBackgroundTask implements Runnable {
        private final IBackground background;
        private final String path;

        private SetBackgroundTask(IBackground background, String path) {
            this.background = Objects.requireNonNull(background, "background");
            this.path = path;
        }

        @Override
        public void run() {
            hideBackgroundTask.run();

            wrapper.setBackground(background);

            try {
                background.loadBackground(path);
            } catch (OutOfMemoryError outOfMemoryError) {
                ExceptionHandler.reduceMemory(outOfMemoryError);
            } catch (Exception e) {
                LOGGER.error("Could not load background for {}; path: {}", background, path, e);
            }

            showBackgroundTask.run();
        }
    }
}
