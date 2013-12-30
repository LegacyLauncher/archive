package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.settings.Settings;
import java.awt.Choice;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class LaunchActionChoice extends Choice implements LocalizableComponent, SettingsField {
   private static final long serialVersionUID = 7116359806652349614L;
   private Map values = new LinkedHashMap();
   private final SettingsForm sf;
   private Settings l;
   private boolean saveable;

   LaunchActionChoice(SettingsForm settingsform) {
      this.sf = settingsform;
      this.l = this.sf.l;
      this.createList();
   }

   private void createList() {
      this.removeAll();
      GlobalSettings.ActionOnLaunch[] available = GlobalSettings.ActionOnLaunch.values();
      String current = TLauncher.getInstance().getSettings().getActionOnLaunch().toString();
      GlobalSettings.ActionOnLaunch[] var6 = available;
      int var5 = available.length;

      for(int var4 = 0; var4 < var5; ++var4) {
         GlobalSettings.ActionOnLaunch al = var6[var4];
         String value = al.toString();
         String key = this.l.get("settings.launch-action." + value);
         this.values.put(key, value);
         this.add(key);
         if (current.equals(value)) {
            this.select(key);
         }
      }

   }

   public String getValue() {
      return this.getValue(this.getSelectedItem());
   }

   private String getValue(String name) {
      return (String)this.values.get(name);
   }

   public void setValue(String id) {
      if (id == null) {
         id = GlobalSettings.ActionOnLaunch.getDefault().toString();
      }

      Iterator var3 = this.values.entrySet().iterator();

      while(var3.hasNext()) {
         Entry curen = (Entry)var3.next();
         if (((String)curen.getKey()).equals(id)) {
            this.select((String)curen.getValue());
         }
      }

   }

   public void updateLocale() {
      this.createList();
   }

   public String getSettingsPath() {
      return "minecraft.onlaunch";
   }

   public boolean isValueValid() {
      return true;
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
