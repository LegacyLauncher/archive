package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.util.U;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Alert {
   private static boolean show = false;
   private static final int wrap = 90;
   private static final String PREFIX = "TLauncher : ";
   private static final String DEFAULT_TITLE = "An error occurred";
   private static final String DEFAULT_MESSAGE = "An unexpected error occurred";
   private static final boolean DEFAULT_EXIT = false;

   public static void showError(String title, String message, Object textarea, Throwable e, boolean exit) {
      if (!show) {
         show = true;
         JFrame frame = new JFrame();
         String t_title = "TLauncher : " + title;
         String t_message = message != null ? U.w("<html><div align=\"justify\">" + message.replace("\n", "<br/>") + "</div></html>", 90) : null;
         String t_throwable = e != null ? U.stackTrace(e) : null;
         String t_textarea = textarea != null ? textarea.toString() : null;
         AlertPanel panel = new AlertPanel(t_message);
         if (t_textarea != null) {
            panel.addTextArea(t_textarea);
         }

         if (t_throwable != null) {
            panel.addTextArea(t_throwable);
         }

         frame.requestFocus();
         JOptionPane.showMessageDialog(frame, panel, t_title, 0);
         show = false;
         if (exit) {
            System.exit(1);
         }

      }
   }

   public static void showError(String title, String message, Throwable e) {
      showError(title, message, (Object)null, e, false);
   }

   public static void showError(String message, Throwable e) {
      showError("An error occurred", message, (Object)null, e, false);
   }

   public static void showError(Throwable e, boolean exit) {
      showError("An error occurred", "An unexpected error occurred", (Object)null, e, exit);
   }

   public static void showError(String title, String message, Object textarea) {
      showError(title, message, textarea, (Throwable)null, false);
   }

   public static void showError(String title, String message) {
      showError(title, message, (Object)null, (Throwable)null, false);
   }

   public static void showWarning(String title, String message) {
      if (!show) {
         show = true;
         JFrame frame = new JFrame();
         String t_title = "TLauncher : " + title;
         String t_message = U.w(message, 90);
         frame.requestFocus();
         JOptionPane.showMessageDialog(frame, t_message, t_title, 2);
         show = false;
      }
   }

   public static boolean showQuestion(String title, String message, boolean force) {
      if (!force && show) {
         return false;
      } else {
         show = true;
         JFrame frame = new JFrame();
         String t_title = "TLauncher : " + title;
         frame.requestFocus();
         boolean result = JOptionPane.showConfirmDialog(frame, message, t_title, 0) == 0;
         show = false;
         return result;
      }
   }
}
