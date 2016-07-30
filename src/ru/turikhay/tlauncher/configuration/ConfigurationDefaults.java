package ru.turikhay.tlauncher.configuration;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.launcher.versions.ReleaseType;
import ru.turikhay.util.Direction;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.OS;

public final class ConfigurationDefaults {
   private static WeakReference ref;
   private final HashMap d = new HashMap();

   public static ConfigurationDefaults getInstance() {
      ConfigurationDefaults instance;
      if (ref == null || (instance = (ConfigurationDefaults)ref.get()) == null) {
         instance = new ConfigurationDefaults();
         ref = new WeakReference(instance);
      }

      return instance;
   }

   private ConfigurationDefaults() {
      this.d.put("settings.version", 3);
      this.d.put("minecraft.gamedir", MinecraftUtil.getDefaultWorkingDirectory().getAbsolutePath());
      this.d.put("minecraft.gamedir.separate", false);
      this.d.put("minecraft.size", new IntegerArray(new int[]{925, 530}));
      this.d.put("minecraft.fullscreen", false);
      Iterator var1 = ReleaseType.getDefault().iterator();

      while(var1.hasNext()) {
         ReleaseType type = (ReleaseType)var1.next();
         this.d.put("minecraft.versions." + type.name().toLowerCase(), true);
      }

      this.d.put("minecraft.javaargs", (Object)null);
      this.d.put("minecraft.args", (Object)null);
      this.d.put("minecraft.improvedargs", true);
      this.d.put("minecraft.cmd", (Object)null);
      this.d.put("minecraft.memory", OS.Arch.PREFERRED_MEMORY);
      this.d.put("minecraft.onlaunch", Configuration.ActionOnLaunch.HIDE);
      this.d.put("gui.font", OS.CURRENT == OS.WINDOWS ? 12 : 14);
      this.d.put("gui.size", new IntegerArray(new int[]{935, 570}));
      this.d.put("gui.systemlookandfeel", true);
      this.d.put("gui.background", (Object)null);
      this.d.put("gui.logger", Configuration.LoggerType.getDefault());
      this.d.put("gui.logger.width", 720);
      this.d.put("gui.logger.height", 500);
      this.d.put("gui.logger.x", 30);
      this.d.put("gui.logger.y", 30);
      this.d.put("gui.direction.loginform", Direction.CENTER);
      this.d.put("connection", Configuration.ConnectionQuality.getDefault());
      this.d.put("client", UUID.randomUUID());
      if (OS.WINDOWS.isCurrent()) {
         this.d.put("windows.dxdiag", true);
      }

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
