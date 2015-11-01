package ru.turikhay.tlauncher.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.launcher.versions.ReleaseType;
import ru.turikhay.tlauncher.updater.Notices;
import ru.turikhay.util.Direction;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.OS;

class ConfigurationDefaults {
   private static final int version = 3;
   private final HashMap d = new HashMap();

   ConfigurationDefaults() {
      this.d.put("settings.version", 3);
      this.d.put("login.auto", false);
      this.d.put("login.auto.timeout", 3);
      this.d.put("minecraft.gamedir", MinecraftUtil.getDefaultWorkingDirectory().getAbsolutePath());
      this.d.put("minecraft.size", new IntegerArray(new int[]{925, 530}));
      this.d.put("minecraft.fullscreen", false);
      Iterator i$ = ReleaseType.getDefault().iterator();

      while(i$.hasNext()) {
         ReleaseType type = (ReleaseType)i$.next();
         this.d.put("minecraft.versions." + type.name().toLowerCase(), true);
      }

      i$ = ReleaseType.SubType.getDefault().iterator();

      while(i$.hasNext()) {
         ReleaseType.SubType subType = (ReleaseType.SubType)i$.next();
         this.d.put("minecraft.versions.sub." + subType.name().toLowerCase(), true);
      }

      Notices.NoticeType[] var4;
      int var3 = (var4 = Notices.NoticeType.values()).length;

      for(int var7 = 0; var7 < var3; ++var7) {
         Notices.NoticeType var6 = var4[var7];
         if (var6.isAdvert()) {
            this.d.put("gui.notice." + var6.name().toLowerCase(), true);
         }
      }

      this.d.put("minecraft.improvedargs", true);
      this.d.put("minecraft.memory", OS.Arch.PREFERRED_MEMORY);
      this.d.put("minecraft.onlaunch", Configuration.ActionOnLaunch.getDefault());
      this.d.put("gui.font", OS.CURRENT == OS.WINDOWS ? 12 : 14);
      this.d.put("gui.size", new IntegerArray(new int[]{925, 550}));
      this.d.put("gui.console", Configuration.ConsoleType.getDefault());
      this.d.put("gui.console.width", 720);
      this.d.put("gui.console.height", 500);
      this.d.put("gui.console.x", 30);
      this.d.put("gui.console.y", 30);
      this.d.put("gui.direction.loginform", Direction.CENTER);
      this.d.put("gui.systemlookandfeel", true);
      this.d.put("connection", Configuration.ConnectionQuality.getDefault());
      this.d.put("client", UUID.randomUUID());
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
