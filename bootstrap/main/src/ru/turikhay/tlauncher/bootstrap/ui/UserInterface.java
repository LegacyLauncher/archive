package ru.turikhay.tlauncher.bootstrap.ui;

import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.task.TaskListener;
import ru.turikhay.tlauncher.bootstrap.task.TaskListenerAdapter;
import ru.turikhay.tlauncher.bootstrap.util.U;
import shaded.org.apache.commons.lang3.builder.ToStringBuilder;
import shaded.org.apache.commons.lang3.builder.ToStringStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class UserInterface {
    private final JFrame frame;
    private final JLabel iconLabel;
    private final JProgressBar progressBar;

    private final TaskListener taskListener;

    public UserInterface() throws HeadlessException {
        if(!isHeaded()) {
            throw new HeadlessException();
        }

        this.frame = new JFrame();
        try {
            frame.setType(Window.Type.UTILITY);
        } catch(Error incompatibleError) {
            // ignore
        }

        frame.getContentPane().setLayout(new BorderLayout());

        this.iconLabel = new JLabel();
        iconLabel.setIcon(new ImageIcon(getClass().getResource("icon.png")));
        iconLabel.setOpaque(false);
        frame.getContentPane().add(iconLabel, BorderLayout.WEST);

        this.progressBar = new JProgressBar();
        progressBar.setOpaque(false);
        frame.getContentPane().add(progressBar, BorderLayout.CENTER);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(bindingTask != null && bindingTask.isExecuting()) {
                    bindingTask.interrupt();
                }
            }
        });

        this.taskListener = new TaskListenerAdapter() {
            @Override
            public void onTaskStarted(Task task) {
                log("Task started");

                if(frame.isDisplayable()) {
                    frame.setLocationRelativeTo(null);
                    frame.setAlwaysOnTop(true);
                    frame.setVisible(true);

                    progressBar.setValue(-1);
                }
            }

            @Override
            public void onTaskUpdated(Task task, double percentage) {
                if(frame.isDisplayable()) {
                    int newValue = percentage < 0. ? -1 : (int) (percentage * 100.);
                    if (progressBar.getValue() - newValue != 0) {
                        log("Task updated:", percentage);
                        progressBar.setValue(newValue);
                    }
                    if (percentage == 1.) {
                        onTaskSucceeded(task);
                    }
                }
            }

            @Override
            public void onTaskInterrupted(Task task) {
                log("Task interrupted");
                if(frame.isDisplayable()) {
                    frame.dispose();
                }
            }

            @Override
            public void onTaskSucceeded(Task task) {
                log("Task succeed");
                if(frame.isDisplayable()) {
                    progressBar.setValue(100);
                    frame.dispose();
                }
            }
        };

        frame.pack();
    }

    public JFrame getFrame() {
        return frame;
    }

    private Task bindingTask;
    public void bindToTask(Task task) {
        if(this.bindingTask != null && this.bindingTask.isExecuting()) {
            throw new IllegalStateException();
        }

        this.bindingTask = task;
        if(this.bindingTask != null) {
            this.bindingTask.addListener(taskListener);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("task", bindingTask)
                .build();
    }

    public static void showError(String message, Object textarea) {
        if(isHeaded()) {
            Alert.showError(message, textarea);
        }
    }

    private static boolean isHeaded() {
        return !GraphicsEnvironment.isHeadless();
    }

    private static void log(Object...o) {
        U.log("[UI]", o);
    }
}
