package com.turikhay.tlauncher.ui.swing;

import com.turikhay.tlauncher.ui.loc.Localizable;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;
import net.minecraft.launcher.versions.VersionSource;

public class VersionCellRenderer implements ListCellRenderer {
   public static final VersionSyncInfo LOADING = new VersionSyncInfo((Version)null, (Version)null, (Version)null, false, false, (VersionSource)null, (VersionSource)null);
   public static final VersionSyncInfo EMPTY = new VersionSyncInfo((Version)null, (Version)null, (Version)null, false, false, (VersionSource)null, (VersionSource)null);
   protected final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$net$minecraft$launcher$versions$ReleaseType;

   public Component getListCellRendererComponent(JList list, VersionSyncInfo value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel renderer = (JLabel)this.defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      renderer.setAlignmentY(0.5F);
      if (value == null) {
         renderer.setText("(null)");
      } else if (value.equals(LOADING)) {
         renderer.setText(Localizable.get("versions.loading"));
      } else if (value.equals(EMPTY)) {
         renderer.setText(Localizable.get("versions.notfound.tip"));
      } else {
         String id = value.getId();
         String label;
         switch($SWITCH_TABLE$net$minecraft$launcher$versions$ReleaseType()[value.getLatestVersion().getType().ordinal()]) {
         case 1:
            label = Localizable.get("version.release", id);
            break;
         case 2:
            label = Localizable.get("version.snapshot", id);
            break;
         case 3:
            label = Localizable.get("version.old-beta", id.substring(1));
            break;
         case 4:
            label = Localizable.get("version.old-alpha", id.startsWith("a") ? id.substring(1) : id);
            break;
         default:
            label = id;
         }

         renderer.setText(label);
      }

      return renderer;
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$net$minecraft$launcher$versions$ReleaseType() {
      int[] var10000 = $SWITCH_TABLE$net$minecraft$launcher$versions$ReleaseType;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[ReleaseType.values().length];

         try {
            var0[ReleaseType.MODIFIED.ordinal()] = 6;
         } catch (NoSuchFieldError var7) {
         }

         try {
            var0[ReleaseType.OLD.ordinal()] = 5;
         } catch (NoSuchFieldError var6) {
         }

         try {
            var0[ReleaseType.OLD_ALPHA.ordinal()] = 4;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[ReleaseType.OLD_BETA.ordinal()] = 3;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[ReleaseType.RELEASE.ordinal()] = 1;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[ReleaseType.SNAPSHOT.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[ReleaseType.UNKNOWN.ordinal()] = 7;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$net$minecraft$launcher$versions$ReleaseType = var0;
         return var0;
      }
   }
}
