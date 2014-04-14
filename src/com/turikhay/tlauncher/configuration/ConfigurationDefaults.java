package com.turikhay.tlauncher.configuration;

import com.turikhay.util.IntegerArray;
import com.turikhay.util.MinecraftUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.launcher.versions.ReleaseType;

class ConfigurationDefaults {
   private static final int version = 3;
   private final Map d = new HashMap();

   ConfigurationDefaults() {
      this.d.put("settings.version", 3);
      this.d.put("login.auto", false);
      this.d.put("login.auto.timeout", 3);
      this.d.put("minecraft.gamedir", MinecraftUtil.getDefaultWorkingDirectory().getAbsolutePath());
      this.d.put("minecraft.size", new IntegerArray(new int[]{925, 530}));
      ReleaseType[] var4;
      int var3 = (var4 = ReleaseType.getDefinable()).length;

      for(int var2 = 0; var2 < var3; ++var2) {
         ReleaseType type = var4[var2];
         this.d.put("minecraft.versions." + type, true);
      }

      this.d.put("minecraft.onlaunch", Configuration.ActionOnLaunch.getDefault());
      this.d.put("gui.console", Configuration.ConsoleType.getDefault());
      this.d.put("gui.console.width", 720);
      this.d.put("gui.console.height", 500);
      this.d.put("gui.console.x", 30);
      this.d.put("gui.console.y", 30);
      this.d.put("connection", Configuration.ConnectionQuality.getDefault());
   }

   public static int getVersion() {
      return 3;
   }

   public Map getMap() {
      return Collections.unmodifiableMap(this.d);
   }

   public Object get(String key) {
      return this.d.get(key);
   }
}
