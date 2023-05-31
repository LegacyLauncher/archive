package ru.turikhay.tlauncher.bootstrap.ui;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.tlauncher.bootstrap.exception.FatalExceptionType;
import ru.turikhay.tlauncher.bootstrap.meta.UpdateMeta;
import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.task.TaskListener;
import ru.turikhay.tlauncher.bootstrap.ui.swing.SwingImageIcon;
import ru.turikhay.tlauncher.bootstrap.util.U;
import ru.turikhay.tlauncher.bootstrap.util.UTF8Control;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class UserInterface implements IInterface {
    public static final String DEFAULT_LOCALE = "en_US";
    static final int BORDER_SIZE = 20, TASK_DEPTH = 2;

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

    private static final NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance();

    private final JFrame frame;
    private final JPanel panel;
    private final JProgressBar progressBar;

    private final TaskListener<Object> taskListener;

    private UserInterface() throws HeadlessException {
        if (!isHeaded()) {
            throw new HeadlessException();
        }

        this.frame = new JFrame();
        frame.setIconImages(
                Stream.of("16", "64", "128", "256")
                        .map(r -> getClass().getResource("icon-" + r + ".png"))
                        .map(SwingImageIcon::loadImage)
                        .collect(Collectors.toList())
        );
        frame.setResizable(false);

        panel = new JPanel();
        panel.setOpaque(false);
        frame.getContentPane().add(panel);

        BorderLayout layout = new BorderLayout();
        layout.setHgap(BORDER_SIZE);
        layout.setVgap(BORDER_SIZE);
        panel.setLayout(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));

        JLabel iconLabel = new JLabel();
        iconLabel.setIcon(SwingImageIcon.loadIcon(getClass().getResource("icon-256.png"), 48, 48));
        iconLabel.setOpaque(false);
        iconLabel.setPreferredSize(new Dimension(48, 48));
        iconLabel.setVerticalAlignment(SwingConstants.TOP);
        panel.add(iconLabel, BorderLayout.WEST);

        this.progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(300, 0));
        progressBar.setOpaque(false);
        panel.add(progressBar, BorderLayout.CENTER);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (bindingTask != null && bindingTask.isExecuting()) {
                    bindingTask.interrupt();
                }
            }
        });

        this.taskListener = new TaskListener<Object>() {
            @Override
            public void onTaskStarted(Task<?> task) {
                log("Task started");

                if (frame.isDisplayable()) {
                    frame.setLocationRelativeTo(null);
                    //frame.setAlwaysOnTop(true);
                    frame.setVisible(true);

                    progressBar.setValue(0);
                    progressBar.setIndeterminate(true);
                }
            }

            @Override
            public void onTaskUpdated(Task<?> task, double percentage) {
                if (frame.isDisplayable()) {
                    int newValue = percentage < 0. ? -1 : (int) (percentage * 100.);
                    if (progressBar.getValue() - newValue != 0) {
                        log("Task updated:", percentage);

                        Task<?> childTask = getChildTask(task, TASK_DEPTH);
                        if (childTask.getProgress() < 0) {
                            progressBar.setIndeterminate(true);
                        } else {
                            progressBar.setIndeterminate(false);
                            progressBar.setValue(newValue);
                        }

                        String title = getLString("appname", "Bootstrap") + " :: " +
                                (newValue == -1 ? "..." : PERCENT_FORMAT.format(newValue / 100.)) + " :: " +
                                getLocalizedTaskName(childTask);

                        frame.setTitle(title);
                    }
                    if (percentage == 1.) {
                        onTaskSucceeded(task);
                    }
                }
            }

            @Override
            public void onTaskInterrupted(Task<?> task) {
                log("Task interrupted");
                if (frame.isDisplayable()) {
                    frame.dispose();
                }
            }

            @Override
            public void onTaskSucceeded(Task<?> task) {
                log("Task succeed");
                if (frame.isDisplayable()) {
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

    private Task<?> bindingTask;

    @Override
    public void bindToTask(Task<?> task) {
        if (this.bindingTask != null && this.bindingTask.isExecuting()) {
            throw new IllegalStateException();
        }

        this.bindingTask = task;
        if (this.bindingTask != null) {
            this.bindingTask.addListener(taskListener);
        }
    }

    @Override
    public void dispose() {
        getFrame().dispose();
    }

    public UpdateMeta.ConnectionInterrupter createInterrupter() {
        return callback -> SwingUtilities.invokeLater(() -> {
            JButton button = new JButton(getLString("skip", "Skip"));
            button.addActionListener(e -> {
                panel.remove(button);
                panel.revalidate();
                panel.repaint();
                callback.onConnectionInterrupted();
            });
            panel.add(button, BorderLayout.EAST);
            panel.revalidate();
            panel.repaint();
        });
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("task", bindingTask)
                .build();
    }

    public static UserInterface createInterface() throws InterruptedException {
        AtomicReference<UserInterface> ref = new AtomicReference<>();
        try {
            SwingUtilities.invokeAndWait(() -> {
                ref.set(new UserInterface());
            });
        } catch (InvocationTargetException e) {
            throw new RuntimeException("couldn't init UserInterface", e);
        }
        return ref.get();
    }

    public static ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public static String getLocale() {
        return getLString("locale", "en_US");
    }

    public static String getLString(String key, String defaultValue) {
        final ResourceBundle b = resourceBundle;
        return b == null ? defaultValue : b.containsKey(key) ? b.getString(key) : defaultValue;
    }

    public static void showError(String message, Object textarea) {
        if (isHeaded()) {
            Alert.showError(message, textarea);
        } else {
            HeadlessInterface.printError(message, textarea);
        }
    }

    public static void showWarning(String message, Object textarea) {
        if (isHeaded()) {
            Alert.showWarning(message, textarea);
        } else {
            HeadlessInterface.printWarning(message, textarea);
        }
    }

    public static void showFatalError(FatalExceptionType type) {
        if (isHeaded()) {
            FatalExceptionHandler.handle(type);
        } else {
            HeadlessInterface.printFatalException(type);
        }
    }

    private static boolean headed = !GraphicsEnvironment.isHeadless();

    public static boolean isHeaded() {
        return headed;
    }

    public static void setHeaded(boolean head) {
        if (GraphicsEnvironment.isHeadless() && head) {
            throw new HeadlessException("current instance is headless");
        }
        UserInterface.headed = head;
    }

    static String getLocalizedTaskName(Task<?> task) {
        Objects.requireNonNull(task, "task");
        return getLString("loading.task." + task.getName(), task.getName());
    }

    static Task<?> getChildTask(Task<?> task, int depth) {
        Task<?> child = task.getBindingTask();
        if (child == null || depth == 0) {
            return task;
        }
        return getChildTask(child, depth - 1);
    }

    public static void setSystemLookAndFeel() {
        String systemLaf = UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(systemLaf);
        } catch (Exception e) {
            log("Couldn't set system L&F:", systemLaf, e);
        }
    }

    private static void log(Object... o) {
        U.log("[UI]", o);
    }
}
