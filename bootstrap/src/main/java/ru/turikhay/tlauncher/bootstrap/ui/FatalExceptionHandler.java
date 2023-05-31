package ru.turikhay.tlauncher.bootstrap.ui;

import ru.turikhay.tlauncher.bootstrap.exception.FatalExceptionType;
import ru.turikhay.tlauncher.bootstrap.ui.message.*;

import java.util.ResourceBundle;

final class FatalExceptionHandler {

    private final ResourceBundle b = UserInterface.getResourceBundle();
    private final FatalExceptionType exceptionType;
    private final MessageHost host;

    private FatalExceptionHandler(FatalExceptionType exceptionType) {
        this.exceptionType = exceptionType;
        this.host = new MessageHost();
    }

    void showMessage() {
        host.setTitle(b.getString("fatal.title"));
        host.showMessage(initialErrorMessage());
    }

    private Message initialErrorMessage() {
        String text = getInitialText();
        return new BiButtonMessage(text, contactButton(), createCloseButton());
    }

    private Button contactButton() {
        boolean canUsePasta = canUsePasta();
        return new Button(
                b.getString("fatal.buttons." + (canUsePasta ? "send_logs" : "contact")),
                canUsePasta ? pastaAction() : this::contactDirectly);
    }

    private Runnable pastaAction() {
        return new PastaAction(host);
    }

    private void contactDirectly() {
        host.showMessage(new BiButtonMessage(
                b.getString("contact-directly.save-logs"),
                new Button(
                        b.getString("contact-directly.save-logs.yes"),
                        () -> new SaveLogsAction(host).run()
                ),
                new Button(
                        b.getString("contact-directly.save-logs.no"),
                        this::showContacts
                )
        ));
    }

    private void showContacts() {
        host.showMessage(new TextMessage(
                b.getString("contact-directly")
                        + "<br/>"
                        + b.getString("contacts")
        ));
    }

    private Button createCloseButton() {
        return new Button(b.getString("fatal.buttons.close"), host::close);
    }

    private String getInitialText() {
        boolean isUnknown = false;
        switch (exceptionType) {
            case FILE_LOCKED:
            case UNKNOWN:
                isUnknown = true;
        }
        FatalExceptionType effective = isUnknown ? FatalExceptionType.UNKNOWN : exceptionType;
        return b.getString("fatal.type." + effective.nameLowerCase())
                + "<br/><br/>"
                + b.getString("fatal.bottom." + (canUsePasta() ? "pasta" : "contact_directly"));
    }

    private boolean canUsePasta() {
        boolean canUsePasta = true;
        switch (exceptionType) {
            case INTERNET_CONNECTIVITY_BLOCKED:
            case CORRUPTED_INSTALLATION:
            case INTERNET_CONNECTIVITY:
                canUsePasta = false;
        }
        return canUsePasta;
    }

    void waitForClose() {
        try {
            host.waitForClose();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore state
        }
    }

    static void handle(FatalExceptionType type) {
        FatalExceptionHandler handler = new FatalExceptionHandler(type);
        handler.showMessage();
        handler.waitForClose();
    }
}
