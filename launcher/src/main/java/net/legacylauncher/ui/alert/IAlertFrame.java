package net.legacylauncher.ui.alert;

public interface IAlertFrame {
    void init(String title, String text, String copyableText, int messageType);
    void showAlert();
    boolean isYes();
}
