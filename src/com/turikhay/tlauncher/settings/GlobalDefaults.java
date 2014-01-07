package com.turikhay.tlauncher.settings;

import com.turikhay.util.IntegerArray;
import com.turikhay.util.MinecraftUtil;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.launcher.OperatingSystem;

public class GlobalDefaults {
   private Map d = new HashMap();

   GlobalDefaults(GlobalSettings g) {
      this.d.put("settings.version", g.version);
      this.d.put("login.auto", false);
      this.d.put("login.auto.timeout", 3);
      this.d.put("minecraft.gamedir", MinecraftUtil.getDefaultWorkingDirectory().getAbsolutePath());
      this.d.put("minecraft.javadir", OperatingSystem.getCurrentPlatform().getJavaDir());
      this.d.put("minecraft.size", new IntegerArray(new int[]{925, 530}));
      this.d.put("minecraft.versions.snapshots", true);
      this.d.put("minecraft.versions.beta", false);
      this.d.put("minecraft.versions.alpha", false);
      this.d.put("minecraft.versions.cheats", false);
      this.d.put("minecraft.onlaunch", GlobalSettings.ActionOnLaunch.getDefault());
      this.d.put("gui.console", GlobalSettings.ConsoleType.NONE);
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
