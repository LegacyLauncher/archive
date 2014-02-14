package com.turikhay.tlauncher.ui.converter;

import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.tlauncher.ui.loc.LocalizableStringConverter;
import net.minecraft.launcher.updater.VersionManager;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;
import net.minecraft.launcher.versions.VersionSource;

public class VersionConverter extends LocalizableStringConverter {
   public static final VersionSyncInfo LOADING = new VersionSyncInfo((Version)null, (Version)null, (Version)null, false, false, (VersionSource)null, (VersionSource)null);
   public static final VersionSyncInfo EMPTY = new VersionSyncInfo((Version)null, (Version)null, (Version)null, false, false, (VersionSource)null, (VersionSource)null);
   private final VersionManager vm;

   public VersionConverter(VersionManager vm) {
      super((String)null);
      if (vm == null) {
         throw new NullPointerException();
      } else {
         this.vm = vm;
      }
   }

   public String toString(VersionSyncInfo from) {
      if (from == null) {
         return null;
      } else if (from.equals(LOADING)) {
         return Localizable.get("versions.loading");
      } else if (from.equals(EMPTY)) {
         return Localizable.get("versions.notfound.tip");
      } else {
         String id = from.getId();
         ReleaseType type = from.getLatestVersion().getType();
         if (type != null && !type.equals(ReleaseType.UNKNOWN)) {
            String typeF = type.toString().toLowerCase();
            String formatted = Localizable.get().nget("version." + typeF, id);
            return formatted == null ? id : formatted;
         } else {
            return id;
         }
      }
   }

   public VersionSyncInfo fromString(String from) {
      return this.vm.getVersionSyncInfo(from);
   }

   public String toValue(VersionSyncInfo from) {
      return null;
   }

   public String toPath(VersionSyncInfo from) {
      return null;
   }
}
