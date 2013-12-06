package com.turikhay.tlauncher.ui;

import com.turikhay.util.IntegerArray;
import java.awt.LayoutManager;
import javax.swing.BoxLayout;
import javax.swing.JTextField;

public class ResolutionField extends BlockablePanel implements SettingsField {
   private static final long serialVersionUID = 2840605102354193923L;
   JTextField w;
   JTextField h;

   ResolutionField(SettingsForm sf) {
      LayoutManager layout = new BoxLayout(this, 0);
      this.setLayout(layout);
      this.w = new JTextField();
      this.h = new JTextField();
      this.w.addFocusListener(sf.restart);
      this.h.addFocusListener(sf.restart);
      this.add(this.w);
      this.add(this.h);
   }

   private void setValues(int width, int height) {
      if (width < 1) {
         width = 925;
      }

      if (height < 1) {
         height = 525;
      }

      this.w.setText(String.valueOf(width));
      this.h.setText(String.valueOf(height));
   }

   private int[] getValues() {
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

   public void setEnabled(boolean b) {
      if (b) {
         this.unblockElement((Object)null);
      } else {
         this.blockElement((Object)null);
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

   public String getSettingsPath() {
      return "minecraft.size";
   }

   public String getValue() {
      int[] v = this.getValues();
      return v == null ? null : v[0] + ";" + v[1];
   }

   public boolean isValueValid() {
      return this.getValues() != null;
   }

   public void setValue(String value) {
      int w;
      int h;
      try {
         IntegerArray values = IntegerArray.parseIntegerArray(value);
         w = values.get(0);
         h = values.get(1);
      } catch (Exception var5) {
         h = -1;
         w = -1;
      }

      this.setValues(w, h);
   }

   public void setToDefault() {
      this.setValue((String)null);
   }
}
