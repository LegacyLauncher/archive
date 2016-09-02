package ru.turikhay.tlauncher;

import net.minecraft.launcher.process.JavaProcess;
import net.minecraft.launcher.process.JavaProcessLauncher;
import net.minecraft.launcher.process.JavaProcessListener;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.exceptions.TLauncherException;
import ru.turikhay.tlauncher.ui.LoadingFrame;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

public final class Bootstrapper {
    private static final String MAIN_CLASS = "ru.turikhay.tlauncher.TLauncher", CLASS_LOAD_ERROR = "Error: Could not find or load main class";
    private static final int MAX_MEMORY = 176;
    private static final File DIRECTORY = new File(".");
    private final JavaProcessLauncher processLauncher;
    private final LoadingFrame frame;
    private final Bootstrapper.BootstrapperListener listener;
    private JavaProcess process;
    private boolean terminate = true, started;

    public static void main(String[] args) {
        checkRunningPath();

        try {
            (new Bootstrapper(args)).start();
        } catch (IOException var2) {
            var2.printStackTrace();
            TLauncher.main(args);
        }

    }

    public static JavaProcessLauncher createLauncher(String[] args, boolean loadAdditionalArgs) {
        JavaProcessLauncher processLauncher = new JavaProcessLauncher(null, new String[0]);
        processLauncher.directory(DIRECTORY);
        processLauncher.addCommand("-Xmx" + MAX_MEMORY + "m");
        processLauncher.addCommand("-cp", FileUtil.getRunningJar());
        processLauncher.addCommand(MAIN_CLASS);
        if (args != null && args.length > 0) {
            processLauncher.addCommands(args);
        }

        if (loadAdditionalArgs) {
            File argsFile = new File(DIRECTORY, "tlauncher-" + OS.CURRENT.toString().toLowerCase() + "-" + OS.Arch.CURRENT.toString().toLowerCase() + ".args");
            if (!argsFile.isFile()) {
                argsFile = new File(DIRECTORY, "tlauncher.args");
            }

            if (argsFile.isFile()) {
                String[] extraArgs = loadArgsFromFile(argsFile);
                if (extraArgs != null) {
                    processLauncher.addCommands(extraArgs);
                }
            }
        }

        return processLauncher;
    }

    public static JavaProcessLauncher createLauncher(String[] args) {
        return createLauncher(args, true);
    }

    public Bootstrapper(String[] args) {
        processLauncher = createLauncher(args);

        if (args != null && args.length > 0) {
            for (String arg : args) {
                if (arg == null) {
                    continue;
                }
                if (arg.startsWith("-") && arg.endsWith("no-terminate")) {
                    log("Will only terminate when launcher is closed.");
                    terminate = false;
                    break;
                }
            }
        }

        frame = new LoadingFrame();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                frame.dispose();
                die(0);
            }
        });
        listener = new Bootstrapper.BootstrapperListener();
    }

    public void start() throws IOException {
        if (process != null) {
            throw new IllegalStateException("Process is already started");
        } else {
            log("Starting launcher...");
            process = processLauncher.start();
            process.safeSetExitRunnable(listener);
            frame.setTitle("TLauncher " + TLauncher.getVersion());
            frame.setVisible(true);
        }
    }

    private void die(int status) {
        log("I can be terminated now.");
        if (!started && process.isRunning()) {
            log("...started instance also will be terminated.");
            log("Poka!");
            process.stop();
        }
        System.exit(status);
    }

    private static String[] loadArgsFromFile(File file) {
        log("Loading arguments from file:", file);

        String content;
        try {
            content = FileUtil.readFile(file);
        } catch (IOException var3) {
            log("Cannot load arguments from file:", file);
            return null;
        }

        return StringUtils.split(content, ' ');
    }

    private static void log(Object... s) {
        U.log("[Bootstrapper]", s);
    }

    private class BootstrapperListener implements JavaProcessListener {
        private StringBuffer buffer;

        private BootstrapperListener() {
            buffer = new StringBuffer();
        }

        public void onJavaProcessLog(JavaProcess jp, String line) {
            U.plog('>', line);
            buffer.append(line).append('\n');
            if (line.startsWith("[Loading]")) {
                if (line.length() < "[Loading]".length() + 2) {
                    Bootstrapper.log("Cannot parse line: content is empty.");
                    return;
                }

                String content = line.substring("[Loading]".length() + 1);
                Bootstrapper.LoadingStep step = Reflect.parseEnum(LoadingStep.class, content);
                if (step == null) {
                    Bootstrapper.log("Cannot parse line: cannot parse step");
                    return;
                }

                if (step == LoadingStep.LOADING_FIRSTRUN) {
                    frame.setExtendedState(Frame.ICONIFIED);
                } else {
                    if (frame.getExtendedState() != Frame.ICONIFIED) {
                        frame.setExtendedState(Frame.NORMAL);
                    }
                }

                frame.setProgress(step.percentage);
                if (step.percentage == 100) {
                    started = true;
                    frame.dispose();

                    if (terminate) {
                        die(0);
                    }
                }
            }

        }

        public void onJavaProcessEnded(JavaProcess jp) {
            if (!frame.isVisible()) {
                frame.setVisible(true);
            }

            int exit = jp.getExitCode();
            if (exit != 0) {
                if (CLASS_LOAD_ERROR.equals(buffer.substring(0, CLASS_LOAD_ERROR.length()))) {
                    JOptionPane.showMessageDialog(null,
                            "Could not find or load main class. You can try to place executable file into root folder or download launcher once more using link below." +
                                    "\n" +
                                    "Не удалось загрузить главный класс Java. Попробуйте положить TLauncher в корневую директорию (например, в C:\\) или загрузить\nлаунчер заново, используя ссылку ниже." +
                                    "\n\n" +
                                    "http://tlaun.ch/jar",
                            "Error launching TLauncher", JOptionPane.ERROR_MESSAGE
                    );
                } else {
                    Alert.showError("Error starting TLauncher", "TLauncher application was closed with illegal exit code (" + exit + "). See logger:", buffer.toString());
                }
            }

            die(exit);
        }

        public void onJavaProcessError(JavaProcess jp, Throwable e) {
        }
    }

    public enum LoadingStep {
        INITALIZING(11),
        LOADING_CONFIGURATION(25),
        LOADING_LOOKANDFEEL(35),
        LOADING_LOGGER(40),
        LOADING_FIRSTRUN(45),
        LOADING_MANAGERS(50),
        LOADING_WINDOW(62),
        PREPARING_MAINPANE(77),
        POSTINIT_GUI(82),
        REFRESHING_INFO(91),
        SUCCESS(100);

        public static final String LOADING_PREFIX = "[Loading]";
        public static final String LOADING_DELIMITER = " = ";
        private final int percentage;

        LoadingStep(int percentage) {
            this.percentage = percentage;
        }

        public int getPercentage() {
            return percentage;
        }
    }

    public static void checkRunningPath() {
        String path = Bootstrapper.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        try {
            path = URLDecoder.decode(path, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (path.contains("!")) {
            String message =
                    "Please do not run (any) Java application which path contains folder name that ends with «!»" +
                            "\n" +
                            "Не запускайте Java-приложения в директориях, чей путь содержит «!». Переместите TLauncher в другую папку." +
                            "\n\n" + path;
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
            throw new TLauncherException(message);
        }
    }
}
