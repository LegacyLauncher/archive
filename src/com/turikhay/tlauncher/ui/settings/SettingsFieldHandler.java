package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.ui.block.Blocker;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.FocusListener;

public class SettingsFieldHandler extends SettingsHandler {
   private final SettingsField field;
   private final Component component;

   SettingsFieldHandler(String path, Component comp, FocusListener focus) {
      super(path);
      if (!(comp instanceof SettingsField)) {
         throw new IllegalArgumentException();
      } else {
         if (focus != null) {
            this.addFocus(comp, focus);
         }

         this.component = comp;
         this.field = (SettingsField)comp;
      }
   }

   SettingsFieldHandler(String path, Component comp) {
      this(path, comp, (FocusListener)null);
   }

   public Component getComponent() {
      return this.component;
   }

   public String getValue() {
      return this.field.getSettingsValue();
   }

   protected void setValue0(String s) {
      this.field.setSettingsValue(s);
   }

   public boolean isValid() {
      return this.field.isValueValid();
   }

   private void addFocus(Component comp, FocusListener focus) {
      comp.addFocusListener(focus);
      if (comp instanceof Container) {
         Component[] var6;
         int var5 = (var6 = ((Container)comp).getComponents()).length;

         for(int var4 = 0; var4 < var5; ++var4) {
            Component curComp = var6[var4];
            this.addFocus(curComp, focus);
         }
      }

   }

   public void block(Object reason) {
      Blocker.blockComponents(reason, this.getComponent());
   }

   public void unblock(Object reason) {
      Blocker.unblockComponents(reason, this.getComponent());
   }
}
