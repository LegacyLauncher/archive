package ru.turikhay.tlauncher.ui.frames;

import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.util.SwingUtil;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessFrame<T> extends BActionFrame {
    private final ExecutorService service = Executors.newCachedThreadPool();
    private volatile Process process, succeededProcess;

    public ProcessFrame() {
        setMinimumSize(SwingUtil.magnify(new Dimension(500, 1)));
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                if (process != succeededProcess) {
                    onCancelled();
                }
                process = null;
                succeededProcess = null;
            }
        });

        ProgressBar progress = new ProgressBar();
        progress.setPreferredSize(new Dimension(1, SwingUtil.magnify(32)));
        progress.setIndeterminate(true);
        getBody().setCenter(progress);
    }

    public final void setIcon(String path) {
        getBody().setWest(Images.getIcon32(path));
    }

    public final void submit(Process process) {
        this.process = process;

        if (process != null) {
            service.submit(process);
        } else {
            onCancelled();
        }
    }

    protected void onProcessing(Process process) {
        checkIfCurrent(process);
        showAtCenter();
    }

    protected void onSucceeded(Process process, T result) {
        checkIfCurrent(process);
        succeededProcess = process;
        setVisible(false);
    }

    protected void onFailed(Process process, Exception e) {
        checkIfCurrent(process);
        setVisible(false);
    }

    protected void onCancelled() {
        setVisible(false);
    }

    protected void checkIfCurrent(Process process) throws IllegalStateException {
        if (this.process != process) {
            throw new IllegalStateException();
        }
    }

    public abstract class Process implements Runnable {
        @Override
        public void run() {
            SwingUtil.wait(() -> onProcessing(this));

            T result;
            try {
                result = get();
            } catch (Exception e) {
                SwingUtil.wait(() -> onFailed(this, e));
                return;
            }

            SwingUtil.wait(() -> onSucceeded(this, result));
        }

        protected abstract T get() throws Exception;
    }
}
