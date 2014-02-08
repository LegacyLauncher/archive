package com.turikhay.tlauncher.configuration;

import com.turikhay.util.IntegerArray;
import com.turikhay.util.MinecraftUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.launcher.OperatingSystem;

public class ConfigurationDefaults {
   private static final int version = 1;
   private final Map d = new HashMap();

   ConfigurationDefaults() {
      this.d.put("settings.version", 1);
      this.d.put("login.auto", false);
      this.d.put("login.auto.timeout", 3);
      this.d.put("minecraft.gamedir", MinecraftUtil.getDefaultWorkingDirectory().getAbsolutePath());
      this.d.put("minecraft.javadir", OperatingSystem.getCurrentPlatform().getJavaDir());
      this.d.put("minecraft.size", new IntegerArray(new int[]{925, 530}));
      this.d.put("minecraft.versions.snapshots", true);
      this.d.put("minecraft.versions.beta", false);
      this.d.put("minecraft.versions.alpha", false);
      this.d.put("minecraft.versions.modified", true);
      this.d.put("minecraft.versions.old", true);
      this.d.put("minecraft.onlaunch", Configuration.ActionOnLaunch.getDefault());
      this.d.put("gui.console", Configuration.ConsoleType.getDefault());
      this.d.put("gui.console.width", 620);
      this.d.put("gui.console.height", 400);
      this.d.put("gui.console.x", 1);
      this.d.put("gui.console.y", 1);
      this.d.put("connection", Configuration.ConnectionQuality.getDefault());
   }

   public static int getVersion() {
      return 1;
   }

   public Map getMap() {
      return Collections.unmodifiableMap(this.d);
   }

   public Object get(String key) {
      return this.d.get(key);
   }
}
