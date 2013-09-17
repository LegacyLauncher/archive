package com.turikhay.tlauncher.ui;

import java.awt.LayoutManager;
import java.awt.TextField;
import javax.swing.BoxLayout;

public class ResolutionField extends BlockablePanel {
   private static final long serialVersionUID = 2840605102354193923L;
   TextField w;
   TextField h;

   ResolutionField(SettingsForm sf) {
      LayoutManager layout = new BoxLayout(this, 0);
      this.setLayout(layout);
      this.w = new TextField();
      this.h = new TextField();
      this.w.addFocusListener(sf.restart);
      this.h.addFocusListener(sf.restart);
      this.add(this.w);
      this.add(this.h);
   }

   public void setValues(int width, int height) {
      if (width < 1) {
         width = 925;
      }

      if (height < 1) {
         height = 525;
      }

      this.w.setText(String.valueOf(width));
      this.h.setText(String.valueOf(height));
   }

   public int[] getValues() {
      int[] i = new int[2];

      try {
         i[0] = Integer.parseInt(this.w.getText());
      } catch (Exception var4) {
         return null;
      }

      try {
         i[1] = Integer.parseInt(this.h.getText());
         return i;
      } catch (Exception var3) {
         return null;
      }
   }

   protected void blockElement(Object reason) {
      this.w.setEnabled(false);
      this.h.setEnabled(false);
   }

   protected void unblockElement(Object reason) {
      this.w.setEnabled(true);
      this.h.setEnabled(true);
   }
}
