package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.configuration.LangConfiguration;
import com.turikhay.tlauncher.ui.loc.LocalizableComponent;
import java.awt.Choice;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class LaunchActionChoice extends Choice implements LocalizableComponent, SettingsField {
   private static final long serialVersionUID = 7116359806652349614L;
   private Map values = new LinkedHashMap();
   private final SettingsForm sf;
   private LangConfiguration l;
   private boolean saveable;

   LaunchActionChoice(SettingsForm settingsform) {
      this.sf = settingsform;
      this.l = this.sf.lang;
      this.createList();
   }

   private void createList() {
      this.removeAll();
      Configuration.ActionOnLaunch[] available = Configuration.ActionOnLaunch.values();
      String current = TLauncher.getInstance().getSettings().getActionOnLaunch().toString();
      Configuration.ActionOnLaunch[] var6 = available;
      int var5 = available.length;

      for(int var4 = 0; var4 < var5; ++var4) {
         Configuration.ActionOnLaunch al = var6[var4];
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
         id = Configuration.ActionOnLaunch.getDefault().toString();
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
