package com.turikhay.tlauncher.settings;

import com.turikhay.util.IntegerArray;
import java.util.HashMap;
import java.util.Map;

public class GlobalDefaults {
   private Map d = new HashMap();

   GlobalDefaults(GlobalSettings g) {
      this.d.put("settings.version", g.version);
      this.d.put("login.auto", false);
      this.d.put("login.auto.timeout", 3);
      this.d.put("minecraft.size", new IntegerArray(new int[]{925, 530}));
      this.d.put("minecraft.versions.snapshots", true);
      this.d.put("minecraft.versions.beta", true);
      this.d.put("minecraft.versions.alpha", true);
      this.d.put("minecraft.versions.cheats", true);
      this.d.put("minecraft.onlaunch", GlobalSettings.ActionOnLaunch.getDefault());
      this.d.put("gui.sun", true);
      this.d.put("gui.console.width", 620);
      this.d.put("gui.console.height", 400);
      this.d.put("gui.console.x", 1);
      this.d.put("gui.console.y", 1);
      this.d.put("timeout.connection", 15000);
      this.d.put("locale", GlobalSettings.getSupported());
   }

   public Map getMap() {
      return this.d;
   }

   public Object get(String key) {
      return this.d.get(key);
   }
}
