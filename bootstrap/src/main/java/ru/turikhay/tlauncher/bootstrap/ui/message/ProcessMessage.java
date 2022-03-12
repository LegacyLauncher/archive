package ru.turikhay.tlauncher.bootstrap.ui.message;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProcessMessage extends TextMessage {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Callable<Void> process;
    private Future<Void> future;

    public ProcessMessage(String text, Callable<Void> process) {
        super(text);
        this.process = process;
    }

    public ProcessMessage(String text, final Runnable r) {
        this(text, () -> {
            r.run();
            return null;
        });
    }

    @Override
    void setupComponents(MessagePanel p) {
        super.setupComponents(p, true);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        p.add(progressBar, BorderLayout.SOUTH);
    }

    @Override
    protected void messageShown(MessagePanel panel) {
        future = executor.submit(process);
    }

    @Override
    protected void messageClosed(MessagePanel panel) {
        if (!future.isDone()) {
            future.cancel(true);
        }
        future = null;
    }
}
