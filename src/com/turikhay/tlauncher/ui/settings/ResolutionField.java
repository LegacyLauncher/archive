package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.ui.block.BlockablePanel;
import com.turikhay.tlauncher.ui.text.InvalidateTextField;
import com.turikhay.util.IntegerArray;
import java.awt.LayoutManager;
import javax.swing.BoxLayout;

public class ResolutionField extends BlockablePanel implements SettingsField {
   private static final long serialVersionUID = 2840605102354193923L;
   InvalidateTextField w;
   InvalidateTextField h;
   private final SettingsForm sf;
   private boolean saveable;

   ResolutionField(SettingsForm sf) {
      this.sf = sf;
      LayoutManager layout = new BoxLayout(this, 0);
      this.setLayout(layout);
      this.setOpaque(false);
      this.w = new InvalidateTextField(sf);
      this.h = new InvalidateTextField(sf);
      this.w.addFocusListener(sf.restart);
      this.h.addFocusListener(sf.restart);
      this.add(this.w);
      this.add(this.h);
   }

   private void setValues(int width, int height) {
      int[] def = this.sf.global.getDefaultWindowSize();
      if (width < 1) {
         width = def[0];
      }

      if (height < 1) {
         height = def[1];
      }

      this.w.setText(String.valueOf(width));
      this.h.setText(String.valueOf(height));
   }

   private int[] getValues() {
      int[] i = new int[2];

      try {
         i[0] = Integer.parseInt(this.w.getValue());
      } catch (Exception var4) {
         return null;
      }

      try {
         i[1] = Integer.parseInt(this.h.getValue());
         return i;
      } catch (Exception var3) {
         return null;
      }
   }

   public void setEnabled(boolean b) {
      if (b) {
         this.unblock((Object)null);
      } else {
         this.block((Object)null);
      }

   }

   public void block(Object reason) {
      this.w.setEnabled(false);
      this.h.setEnabled(false);
   }

   public void unblock(Object reason) {
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
      int[] v = this.getValues();
      int[] def = this.sf.global.getDefaultWindowSize();
      boolean valid = v != null;
      if (!valid || v[0] < def[0]) {
         valid = this.w.setInvalid("settings.client.resolution.width.invalid");
      }

      if (v[1] < def[1]) {
         valid |= this.h.setInvalid("settings.client.resolution.height.invalid");
      }

      return valid;
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

   public boolean isSaveable() {
      return this.saveable;
   }

   public void setSaveable(boolean val) {
      this.saveable = val;
   }
}
