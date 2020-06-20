package ru.turikhay.tlauncher.bootstrap.ui;

import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.task.TaskListener;
import ru.turikhay.tlauncher.bootstrap.task.TaskListenerAdapter;
import ru.turikhay.tlauncher.bootstrap.util.U;
import ru.turikhay.tlauncher.bootstrap.util.UTF8Control;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class UserInterface implements IInterface {
    public static final String DEFAULT_LOCALE = "en_US";
    static final int BORDER_SIZE = 5, TASK_DEPTH = 2;

    private static final ResourceBundle resourceBundle;
    static {
        ResourceBundle b = null;
        try {
            b = ResourceBundle.getBundle("bootstrap", new UTF8Control());
        } catch (Exception e) {
            e.printStackTrace();
        }
        resourceBundle = b;
    }

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

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        frame.getContentPane().add(panel);

        BorderLayout layout = new BorderLayout();
        layout.setHgap(BORDER_SIZE / 2);
        layout.setVgap(BORDER_SIZE / 2);
        panel.setLayout(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));

        this.iconLabel = new JLabel();
        iconLabel.setIcon(new ImageIcon(getClass().getResource("icon.png")));
        iconLabel.setOpaque(false);
        panel.add(iconLabel, BorderLayout.WEST);

        this.progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(300, 0));
        progressBar.setOpaque(false);
        panel.add(progressBar, BorderLayout.CENTER);

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
                    //frame.setAlwaysOnTop(true);
                    frame.setVisible(true);

                    progressBar.setValue(0);
                    progressBar.setIndeterminate(true);
                }
            }

            @Override
            public void onTaskUpdated(Task task, double percentage) {
                if(frame.isDisplayable()) {
                    int newValue = percentage < 0. ? -1 : (int) (percentage * 100.);
                    if (progressBar.getValue() - newValue != 0) {
                        log("Task updated:", percentage);

                        Task childTask = getChildTask(task, TASK_DEPTH);
                        if(childTask.getProgress() < 0) {
                            progressBar.setIndeterminate(true);
                        } else {
                            progressBar.setIndeterminate(false);
                            progressBar.setValue(newValue);
                        }

                        StringBuilder title = new StringBuilder();
                        title.append(getLString("appname", "Bootstrap")).append(" :: ");
                        title.append(newValue == -1? "..." : newValue + "%").append(" :: ");
                        title.append(getLocalizedTaskName(childTask));

                        frame.setTitle(title.toString());
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

        frame.setTitle(getLString("appname", "Bootstrap"));
        frame.pack();
    }

    public JFrame getFrame() {
        return frame;
    }

    private Task bindingTask;
    @Override
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
    public void dispose() {
        getFrame().dispose();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("task", bindingTask)
                .build();
    }

    public static ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public static String getLocale() {
        return getLString("locale", "en_US");
    }

    public static String getLString(String key, String defaultValue) {
        final ResourceBundle b = resourceBundle;
        return b == null? defaultValue : b.containsKey(key) ? b.getString(key) : defaultValue;
    }

    public static void showError(String message, Object textarea) {
        if(isHeaded()) {
            Alert.showError(message, textarea);
        } else {
            HeadlessInterface.printError(message, textarea);
        }
    }

    public static void showWarning(String message, Object textarea) {
        if(isHeaded()) {
            Alert.showWarning(message, textarea);
        } else {
            HeadlessInterface.printWarning(message, textarea);
        }
    }

    private static boolean headed = !GraphicsEnvironment.isHeadless();
    public static boolean isHeaded() {
        return headed;
    }

    public static void setHeaded(boolean head) {
        if(GraphicsEnvironment.isHeadless() && head) {
            throw new HeadlessException("current instance is headless");
        }
        UserInterface.headed = head;
    }

    static String getLocalizedTaskName(Task task) {
        U.requireNotNull(task, "task");
        return getLString("loading.task." + task.getName(), task.getName());
    }

    static Task getChildTask(Task task, int depth) {
        Task child = task.getBindingTask();
        if(child == null || depth == 0) {
            return task;
        }
        return getChildTask(child, depth - 1);
    }

    private static void log(Object...o) {
        U.log("[UI]", o);
    }
}
