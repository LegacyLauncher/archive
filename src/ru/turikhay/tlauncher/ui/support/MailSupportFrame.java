package ru.turikhay.tlauncher.ui.support;

import ru.turikhay.tlauncher.ui.alert.Alert;

public class MailSupportFrame extends SupportFrame {
   public MailSupportFrame() {
      super("mail", "mail.png", "mailto:support@tlauncher.ru");
   }

   public void openUrl() {
      Alert.showLocMessage("support.mail.alert", "support@tlauncher.ru");
   }
}
