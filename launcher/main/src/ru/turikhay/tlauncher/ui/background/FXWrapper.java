package ru.turikhay.tlauncher.ui.background;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.Scene;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComponentAdapter;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;
import ru.turikhay.util.async.FxRunnable;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FXWrapper<T extends IFXBackground> extends ExtendedLayeredPane implements ISwingBackground {

    private final Class<T> clazz;

    private FXInitializer init; // null if already initialized
    private JFX wrapper;

    public FXWrapper(Class<T> fxBackground) {
        clazz = fxBackground;

        setOpaque(false);
        setBackground(Color.yellow);

        init = new FXInitializer();

        addComponentListener(new ExtendedComponentAdapter(this, 200) {
            public void onComponentResized(ComponentEvent e) {
                if (wrapper != null) {
                    wrapper.onResize();
                }
            }
        });
    }

    private void init(Runnable runnable, boolean join) {
        final FXInitializer init = this.init;

        if (init == null) {
            //log("FX already initialized");
            runnable.run();
            return;
        }

        if (init.isAlive()) {
            log("FX is initializing so far...");
        } else {
            init.start();
        }

        if (join) {
            try {
                init.join();
            } catch (InterruptedException e) {
                // ignore
            }
            runnable.run();
        } else {
            init.queue.add(runnable);
        }
    }

    private void init(Runnable r) {
        init(r, false);
    }

    @Override
    public void onResize() {
        if (getParent() != null) {
            final Dimension size = getParent().getSize();
            setSize(size);
            log("fxwrapper resized:", size);
        }
    }

    @Override
    public void startBackground() {
        init(new FxRunnable() {
            @Override
            public void runFx() {
                wrapper.background.startBackground();
            }
        });
    }

    @Override
    public void pauseBackground() {
        init(new FxRunnable() {
            @Override
            public void runFx() {
                wrapper.background.pauseBackground();
            }
        });
    }

    @Override
    public void loadBackground(final String path) throws Exception {
        init(new FxRunnable() {
            @Override
            public void runFx() {
                try {
                    wrapper.background.loadBackground(path);
                } catch (Exception e) {
                    log("could not load fx background", path, e);
                }
            }
        }, OS.WINDOWS.isCurrent()); // TODO make special setting to join the fx loading thread?
    }

    private class JFX extends JFXPanel implements ResizeableComponent {
        private T background;

        JFX() throws Exception {
            setBackground(Color.green);

            final Exception[] exception = new Exception[1];

            Platform.setImplicitExit(false);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        background = clazz.newInstance();

                        Scene scene = new Scene((Parent) background, javafx.scene.paint.Color.MAGENTA);
                        scene.setFill(javafx.scene.paint.Color.DARKGRAY);

                        setScene(scene);
                    } catch (Exception e) {
                        log("Could not create background", exception[0] = e);
                    }
                }
            });

            while (background == null && exception[0] == null) {
                U.sleepFor(500);
            }

            if (exception[0] != null) {

                try {
                    Platform.exit();
                } catch (Exception e) {
                    log("Could not exit JavaFX", e);
                }

                throw exception[0];
            }

            FXWrapper.this.add(this);
            onResize();

            log("FX background successfully created", background);
        }

        @Override
        public void onResize() {
            Dimension size = FXWrapper.this.getSize();
            setBounds(0, 0, size.width, size.height);
        }
    }

    private class FXInitializer extends ExtendedThread {
        private final Queue<Runnable> queue = new ConcurrentLinkedQueue<Runnable>();

        @Override
        public void run() {
            checkCurrent();

            init:
            {
                try {
                    log("Initializing...");
                    wrapper = new JFX();
                } catch (Exception e) {
                    log("Could not init FX background", e);
                    break init;
                }
            }

            init = null;

            Runnable task;
            while ((task = queue.poll()) != null) {
                task.run();
            }
        }
    }


    private void log(Object... o) {
        if (Platform.isFxApplicationThread()) {
            U.log("[FXWrapper][in FX]", o);
        } else {
            U.log("[FXWrapper]", o);
        }
    }
}