package ru.turikhay.tlauncher.ui.support;

import ru.turikhay.tlauncher.ui.alert.Alert;

public class MailSupportFrame extends SupportFrame {
    public static final String SUPPORT_MAIL = "support@tlauncher.ru";

    public MailSupportFrame() {
        super("mail", "mail.png", "mailto:" + SUPPORT_MAIL);
    }

    @Override
    public void openUrl() {
        Alert.showLocMessage("support.mail.alert", SUPPORT_MAIL);
    }
}
