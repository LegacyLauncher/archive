package ru.turikhay.tlauncher.jre;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.async.AsyncThread;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class JavaRuntimeInstaller {
    private static final Logger LOGGER = LogManager.getLogger(JavaRuntimeInstaller.class);

    private final JavaRuntimeInstallerProcess process;
    private final Reporter reporter;

    private final AtomicBoolean started = new AtomicBoolean();
    private Future<?> job;

    private JavaRuntimeInstallerListener listener;

    public JavaRuntimeInstaller(JavaRuntimeInstallerProcess process) {
        this.process = process;
        this.reporter = new Reporter();
    }

    public void setListener(JavaRuntimeInstallerListener listener) {
        if(started.get()) {
            throw new IllegalStateException("already started");
        }
        this.listener = listener;
    }

    public void startInstaller() {
        if(started.compareAndSet(false, true)) {
            LOGGER.debug("queueing installer job");
            job = AsyncThread.future(this::doInstall);
        } else {
            throw new IllegalStateException("already started");
        }
    }

    public void interruptInstaller() {
        if(started.get()) {
            LOGGER.debug("interrupting installer");
            job.cancel(true);
        } else {
            throw new IllegalStateException("not started yet");
        }
    }

    private void doInstall() {
        if(listener != null) {
            LOGGER.trace("onJavaInstallerStarted");
            listener.onJavaInstallerStarted();
        }
        try {
            process.install(reporter);
        } catch (InterruptedException interruptedException) {
            LOGGER.debug("Java Installer interrupted", interruptedException);
            if(listener != null) {
                LOGGER.trace("onJavaInstallerInterrupted");
                listener.onJavaInstallerInterrupted();
            }
            return;
        } catch (Exception e) {
            LOGGER.error("Java Installer failed", e);
            if(listener != null) {
                LOGGER.trace("onJavaInstallerFailed");
                listener.onJavaInstallerFailed(e);
            }
            return;
        }
        if(listener != null) {
            LOGGER.trace("onJavaInstallerSucceeded");
            listener.onJavaInstallerSucceeded();
        }
    }

    private class Reporter implements ProgressReporter {
        @Override
        public void reportProgress(long current, long max) {
            if(listener != null) {
                listener.onJavaInstallerProgress(current, max);
            }
        }
    }
}
