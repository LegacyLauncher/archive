package ru.turikhay.tlauncher.ui.alert;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;

public class Alert {
    private static final Logger LOGGER = LogManager.getLogger(Alert.class);
    private static String DEFAULT_TITLE = "An error occurred";
    private static String DEFAULT_MESSAGE = "An unexpected error occurred";

    public static void showError(String title, String message, Object textarea) {
        if (textarea instanceof Throwable) {
            LOGGER.error("Showed this error to the user:", (Throwable) textarea);
        }

        showMonolog(0, title, message, textarea);
    }

    public static void showError(String title, String message) {
        showError(title, message, null);
    }

    public static void showError(String message, Object textarea) {
        showError(DEFAULT_TITLE, message, textarea);
    }

    public static void showError(Object textarea, boolean exit) {
        showError(DEFAULT_TITLE, DEFAULT_MESSAGE, textarea);
        if (exit) {
            System.exit(-1);
        }

    }

    public static void showError(Object textarea) {
        showError(textarea, false);
    }

    public static void showLocError(String titlePath, String messagePath, Object textarea) {
        showError(getLoc(titlePath, "MISSING TITLE"), getLoc(messagePath, "MISSING MESSAGE"), textarea);
    }

    public static void showLocError(String path, Object textarea) {
        showError(getLoc(path + ".title", "MISSING TITLE"), getLoc(path, "MISSING MESSAGE"), textarea);
    }

    public static void showLocError(String path) {
        showLocError(path, null);
    }

    public static void showMessage(String title, String message, Object textarea) {
        showMonolog(1, title, message, textarea);
    }

    public static void showMessage(String title, String message) {
        showMessage(title, message, null);
    }

    public static void showLocMessage(String titlePath, String messagePath, Object textarea) {
        showMessage(getLoc(titlePath, "MISSING TITLE"), getLoc(messagePath, "MISSING MESSAGE"), textarea);
    }

    public static void showLocMessage(String path, Object textarea) {
        showMessage(getLoc(path + ".title", "MISSING TITLE"), getLoc(path, "MISSING MESSAGE"), textarea);
    }

    public static void showLocMessage(String path) {
        showLocMessage(path, null);
    }

    public static void showWarning(String title, String message, Object textarea) {
        showMonolog(2, title, message, textarea);
    }

    public static void showWarning(String title, String message) {
        showWarning(title, message, null);
    }

    public static void showLocWarning(String titlePath, String messagePath, Object textarea) {
        showWarning(getLoc(titlePath, "MISSING TITLE"), getLoc(messagePath, "MISSING MESSAGE"), textarea);
    }

    public static void showLocWarning(String path, Object textarea) {
        showWarning(getLoc(path + ".title", "MISSING TITLE"), getLoc(path, "MISSING MESSAGE"), textarea);
    }

    public static void showLocWarning(String path) {
        showLocWarning(path, null);
    }

    public static boolean showQuestion(String title, String question, Object textarea) {
        return showConfirmDialog(0, 3, title, question, textarea) == 0;
    }

    public static boolean showQuestion(String title, String question) {
        return showQuestion(title, question, null);
    }

    public static boolean showLocQuestion(String titlePath, String questionPath, Object textarea) {
        return showQuestion(getLoc(titlePath, "MISSING TITLE"), getLoc(questionPath, "MISSING QUESTION"), textarea);
    }

    public static boolean showLocQuestion(String titlePath, String questionPath) {
        return showLocQuestion(titlePath, questionPath, null);
    }

    public static boolean showLocQuestion(String path, Object textarea) {
        return showQuestion(getLoc(path + ".title", "MISSING TITLE"), getLoc(path, "MISSING QUESTION"), textarea);
    }

    public static boolean showLocQuestion(String path) {
        return showLocQuestion(path, (Object) null);
    }

    public static String showInputQuestion(String title, String question) {
        return showInputDialog(3, title, question);
    }

    public static String showLocInputQuestion(String titlePath, String questionPath) {
        return showInputQuestion(getLoc(titlePath, "MISSING TITLE"), getLoc(questionPath, "MISSING QUESTION"));
    }

    public static String showLocInputQuestion(String path) {
        return showLocInputQuestion(path + ".title", path);
    }

    private static void showMonolog(int messageType, String title, String message, Object textarea) {
        SwingUtil.wait(() -> JOptionPane.showMessageDialog(null, new AlertPanel(message, textarea), getTitle(title), messageType));
    }

    private static int showConfirmDialog(int optionType, int messageType, String title, String message, Object textarea) {
        return SwingUtil.waitAndReturn(() -> JOptionPane.showConfirmDialog(null, new AlertPanel(message, textarea), getTitle(title), optionType, messageType));
    }

    private static String showInputDialog(int messageType, String title, String message) {
        return SwingUtil.waitAndReturn(() -> JOptionPane.showInputDialog(null, new AlertPanel(message, null), title, messageType));
    }

    public static void prepareLocal() {
        DEFAULT_TITLE = getLoc("alert.error.title", DEFAULT_TITLE);
        DEFAULT_MESSAGE = getLoc("alert.error.message", DEFAULT_MESSAGE);
    }

    private static String getTitle(String title) {
        return "LL : " + (title == null ? "" : title);
    }

    private static String getLoc(String path, String fallbackMessage) {
        String result = Localizable.get(path);
        return result == null ? "" : result;
    }
}
