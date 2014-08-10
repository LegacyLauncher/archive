package ru.turikhay.tlauncher.ui.editor;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.loc.LocalizableHTMLLabel;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.IntegerArray;

public class EditorResolutionField extends BorderPanel implements EditorField {
   private static final long serialVersionUID = -5565607141889620750L;
   private EditorIntegerField w;
   private EditorIntegerField h;
   private ExtendedLabel x;
   private final LocalizableLabel hint;
   private final int[] defaults;

   public EditorResolutionField(String promptW, String promptH, int[] defaults, boolean showDefault) {
      if (defaults == null) {
         throw new NullPointerException();
      } else if (defaults.length != 2) {
         throw new IllegalArgumentException("Illegal array size");
      } else {
         this.defaults = defaults;
         ExtendedPanel container = new ExtendedPanel();
         container.setAlignmentX(0.5F);
         container.setAlignmentY(0.5F);
         this.w = new EditorIntegerField(promptW);
         this.w.setColumns(4);
         this.w.setHorizontalAlignment(0);
         this.h = new EditorIntegerField(promptH);
         this.h.setColumns(4);
         this.h.setHorizontalAlignment(0);
         this.x = new ExtendedLabel("X", 0);
         container.setLayout(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
         c.anchor = 10;
         c.gridx = 0;
         c.weightx = 0.5D;
         c.insets.set(0, 0, 0, 0);
         c.fill = 1;
         container.add(this.w, c);
         c.gridx = 1;
         c.weightx = 0.0D;
         c.insets.set(0, 5, 0, 5);
         c.fill = 3;
         container.add(this.x, c);
         c.gridx = 2;
         c.weightx = 0.5D;
         c.insets.set(0, 0, 0, 0);
         c.fill = 1;
         container.add(this.h, c);
         this.setCenter(container);
         this.hint = new LocalizableHTMLLabel("settings.res.def", new Object[]{defaults[0], defaults[1]});
         this.hint.setFont(this.hint.getFont().deriveFont((float)this.hint.getFont().getSize() - 2.0F));
         if (showDefault) {
            this.setSouth(this.hint);
         }

      }
   }

   public String getSettingsValue() {
      return this.w.getSettingsValue() + ';' + this.h.getSettingsValue();
   }

   int[] getResolution() {
      try {
         IntegerArray arr = IntegerArray.parseIntegerArray(this.getSettingsValue());
         return arr.toArray();
      } catch (Exception var2) {
         return new int[2];
      }
   }

   public boolean isValueValid() {
      int[] size = this.getResolution();
      return size[0] >= 1 && size[1] >= 1;
   }

   public void setSettingsValue(String value) {
      String width;
      String height;
      try {
         IntegerArray arr = IntegerArray.parseIntegerArray(value);
         width = String.valueOf(arr.get(0));
         height = String.valueOf(arr.get(1));
      } catch (Exception var5) {
         width = "";
         height = "";
      }

      this.w.setText(width);
      this.h.setText(height);
   }

   public void setBackground(Color bg) {
      if (this.w != null) {
         this.w.setBackground(bg);
      }

      if (this.h != null) {
         this.h.setBackground(bg);
      }

   }

   public void block(Object reason) {
      Blocker.blockComponents(reason, this.w, this.h);
   }

   public void unblock(Object reason) {
      Blocker.unblockComponents(Blocker.UNIVERSAL_UNBLOCK, this.w, this.h);
   }
}
