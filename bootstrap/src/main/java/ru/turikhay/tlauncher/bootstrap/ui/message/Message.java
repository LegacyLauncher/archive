package ru.turikhay.tlauncher.bootstrap.ui.message;

public abstract class Message {
    abstract void setupComponents(MessagePanel p);

    protected void messageShown(MessagePanel panel) {
    }

    protected void messageClosed(MessagePanel panel) {
    }
}
