package ru.turikhay.tlauncher.ui.swing;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import net.minecraft.launcher.updater.LatestVersionSyncInfo;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.ReleaseType;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

public class VersionCellRenderer implements ListCellRenderer {
   public static final VersionSyncInfo LOADING = VersionSyncInfo.createEmpty();
   public static final VersionSyncInfo EMPTY = VersionSyncInfo.createEmpty();
   private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
   private final int averageColor = (new Color(128, 128, 128, 255)).getRGB();
   private static final ImageIcon ELY_ICON = Images.getIcon("ely.png", SwingUtil.magnify(16));

   public Component getListCellRendererComponent(JList list, VersionSyncInfo value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel mainText = (JLabel)this.defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      mainText.setAlignmentY(0.5F);
      if (value == null) {
         mainText.setText("(null)");
      } else if (value.equals(LOADING)) {
         mainText.setText(Localizable.get("versions.loading"));
      } else if (value.equals(EMPTY)) {
         mainText.setText(Localizable.get("versions.notfound.tip"));
      } else {
         mainText.setText(getLabelFor(value));
         if (this.getShowElyVersions() && TLauncher.getInstance().getElyManager().hasLibraries(value)) {
            mainText.setIcon(ELY_ICON);
         }

         if (!value.isInstalled()) {
            mainText.setBackground(U.shiftColor(mainText.getBackground(), mainText.getBackground().getRGB() < this.averageColor ? 32 : -32));
         }
      }

      return mainText;
   }

   public static String getLabelFor(VersionSyncInfo value) {
      LatestVersionSyncInfo asLatest = value instanceof LatestVersionSyncInfo ? (LatestVersionSyncInfo)value : null;
      ReleaseType type = value.getAvailableVersion().getReleaseType();
      String id;
      String label;
      if (asLatest == null) {
         id = value.getID();
         label = "version." + type;
      } else {
         id = asLatest.getVersionID();
         label = "version.latest." + type;
      }

      label = Localizable.nget(label);
      if (type != null) {
         switch(type) {
         case OLD_BETA:
            id = id.substring(1);
            break;
         case OLD_ALPHA:
            id = id.startsWith("a") ? id.substring(1) : id;
         }
      }

      String text = label != null ? label + " " + id : id;
      return text;
   }

   public boolean getShowElyVersions() {
      return false;
   }
}
