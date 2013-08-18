package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MainInputPanel extends BlockablePanel {
   private static final long serialVersionUID = 296073104610204659L;
   private final LoginForm lf;
   private final Settings l;
   String username;
   TextField field;
   private boolean edit;

   MainInputPanel(LoginForm loginform, String username) {
      this.lf = loginform;
      this.l = this.lf.l;
      LayoutManager lm = new GridLayout(1, 1);
      this.setLayout(lm);
      this.edit = username != null;
      this.field = new TextField(this.edit ? username : this.l.get("loginform.username"), 20);
      this.field.setFont(this.edit ? this.lf.font : this.lf.font_italic);
      this.field.addMouseListener(new MouseListener() {
         public void mouseClicked(MouseEvent arg0) {
            MainInputPanel.this.editUsername();
         }

         public void mouseEntered(MouseEvent arg0) {
         }

         public void mouseExited(MouseEvent arg0) {
         }

         public void mousePressed(MouseEvent arg0) {
         }

         public void mouseReleased(MouseEvent arg0) {
         }
      });
      this.field.addKeyListener(new KeyListener() {
         public void keyPressed(KeyEvent e) {
         }

         public void keyReleased(KeyEvent e) {
            MainInputPanel.this.editUsername();
            MainInputPanel.this.checkUsername();
         }

         public void keyTyped(KeyEvent e) {
         }
      });
      this.field.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            MainInputPanel.this.lf.callLogin();
         }
      });
      this.add(this.field);
      if (this.edit) {
         this.checkUsername();
      }

   }

   private void editUsername() {
      if (!this.edit) {
         this.field.setText("");
         this.field.setFont(this.lf.font);
         this.edit = true;
      }
   }

   boolean checkUsername(boolean notEmpty) {
      String text = this.field.getText();
      String regexp = "^[A-Za-z0-9_-]" + (notEmpty ? "+" : "*") + "$";
      if (text.matches(regexp)) {
         this.usernameOK();
         this.username = text;
         return true;
      } else {
         if (!this.field.hasFocus() && this.username != null) {
            this.field.requestFocusInWindow();
         }

         this.usernameWrong(this.l.get("username.incorrect"));
         return false;
      }
   }

   boolean checkUsername() {
      return this.checkUsername(false);
   }

   void usernameWrong(String reason) {
      this.field.setBackground(Color.pink);
      this.lf.setError(reason);
   }

   void usernameOK() {
      this.field.setBackground(Color.white);
      this.lf.setError((String)null);
   }

   protected void blockElement(Object reason) {
      this.field.setEnabled(false);
   }

   protected void unblockElement(Object reason) {
      this.field.setEnabled(true);
   }
}
