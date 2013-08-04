package com.turikhay.tlauncher.ui;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Alert {
   private static boolean show = false;

   public static void showError(String title, String message, Throwable e, boolean exit) {
      if (!show) {
         show = true;
         JFrame frame = new JFrame();
         String t_title = "TLauncher : " + title;
         String t_message = message + (e != null ? "\n" + stackTrace(e) : "");
         frame.requestFocus();
         JOptionPane.showMessageDialog(frame, t_message, t_title, 0);
         show = false;
         if (exit) {
            System.exit(1);
         }

      }
   }

   public static void showError(String message, Throwable e, boolean exit) {
      showError("An error occurred", message, e, exit);
   }

   public static void showError(Throwable e, boolean exit) {
      showError("An unexpected error occurred", e, exit);
   }

   public static void showError(String title, String message, Throwable e) {
      showError(title, message, e, false);
   }

   public static void showError(String message, Throwable e) {
      showError(message, e, false);
   }

   public static void showError(Throwable e) {
      showError(e, false);
   }

   public static void showError(String title, String message) {
      showError(title, message, (Throwable)null, false);
   }

   public static void showError(String message) {
      showError(message, (Throwable)null, false);
   }

   public static void showWarning(String title, String message) {
      if (!show) {
         show = true;
         JFrame frame = new JFrame();
         String t_title = "TLauncher : " + title;
         frame.requestFocus();
         JOptionPane.showMessageDialog(frame, message, t_title, 2);
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

   private static String stackTrace(Throwable e) {
      String t = e.toString();
      if (t == null) {
         t = "";
      }

      StackTraceElement[] elems = e.getStackTrace();

      for(int x = 0; x < elems.length; ++x) {
         t = t + "\nat " + elems[x].toString();
         if (x >= 5) {
            t = t + "\n... and " + (elems.length - x - 1) + " more";
            break;
         }
      }

      Throwable cause = e.getCause();
      if (cause != null) {
         t = t + "\nCaused by: " + cause.toString();
         StackTraceElement[] causeelems = cause.getStackTrace();

         for(int x = 0; x < causeelems.length; ++x) {
            t = t + "\nat " + causeelems[x].toString();
            if (x >= 5) {
               t = t + "\n... and " + (causeelems.length - x - 1) + " more";
               break;
            }
         }
      }

      return t;
   }
}
