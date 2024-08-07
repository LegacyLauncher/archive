package net.legacylauncher.ui.swing;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.minecraft.auth.Account;
import net.legacylauncher.ui.LegacyLauncherFrame;
import net.legacylauncher.ui.images.ImageIcon;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.ui.theme.Theme;
import net.legacylauncher.util.U;
import net.minecraft.launcher.updater.LatestVersionSyncInfo;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.ReleaseType;

import javax.swing.*;
import java.awt.*;

public class VersionCellRenderer implements ListCellRenderer<VersionSyncInfo> {
    public static final VersionSyncInfo LOADING = VersionSyncInfo.createEmpty();
    public static final VersionSyncInfo EMPTY = VersionSyncInfo.createEmpty();
    private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
    private final int averageColor = U.shiftColor(Theme.getTheme().getForeground(), -128, 64, 128).getRGB();

    private static final net.legacylauncher.ui.images.ImageIcon ELY_ICON = Images.getIcon16("logo-ely");
    private static final net.legacylauncher.ui.images.ImageIcon MOJANG_ICON = Images.getIcon16("logo-mojang");

    public Component getListCellRendererComponent(JList<? extends VersionSyncInfo> list, VersionSyncInfo value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel mainText = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        mainText.setFont(mainText.getFont().deriveFont(LegacyLauncherFrame.getFontSize()));
        mainText.setAlignmentY(0.5F);

        if (value == null) {
            mainText.setText("(null)");
        } else if (value.equals(LOADING)) {
            mainText.setText(Localizable.get("versions.loading"));
        } else if (value.equals(EMPTY)) {
            mainText.setText(Localizable.get("versions.notfound.tip"));
        } else {
            String label = getLabelFor(value);
            int width = mainText.getFontMetrics(mainText.getFont()).stringWidth(label);

            if (getShowVersionsFor() != null && LegacyLauncher.getInstance().getLibraryManager()
                    .hasLibrariesExplicitly(value, getShowVersionsFor().toString())) {
                net.legacylauncher.ui.images.ImageIcon icon = getIconFor(getShowVersionsFor());
                mainText.setIcon(icon);
                if (icon != null) {
                    width += icon.getIconWidth() + mainText.getIconTextGap();
                }
            }

            int prefWidth = list.getFixedCellWidth();

            if (prefWidth > 0 && width >= prefWidth) {
                float fontSize = mainText.getFont().getSize2D();

                Font curFont = null;
                int curWidth;

                while (fontSize > 9) {
                    curFont = mainText.getFont().deriveFont(--fontSize);
                    curWidth = mainText.getFontMetrics(curFont).stringWidth(label);

                    if (curWidth <= prefWidth) {
                        break;
                    }
                }

                if (curFont != null) {
                    mainText.setFont(curFont);
                }
            }

            if (!value.isInstalled()) {
                mainText.setBackground(U.shiftColor(mainText.getBackground(), mainText.getBackground().getRGB() < averageColor ? 32 : -32));
            }

            mainText.setText(label);
        }

        return mainText;
    }

    public static net.legacylauncher.ui.images.ImageIcon getIconFor(Account.AccountType accountType) {
        ImageIcon icon = null;
        switch (accountType) {
            case ELY:
            case ELY_LEGACY:
                icon = ELY_ICON;
                break;
            case MOJANG:
                icon = MOJANG_ICON;
                break;
            case PLAIN:
                break;
        }
        return icon;
    }

    public static String getLabelFor(VersionSyncInfo value) {
        if (value == null) {
            return "(null)";
        } else if (value.equals(LOADING)) {
            return Localizable.get("versions.loading");
        } else if (value.equals(EMPTY)) {
            return Localizable.get("versions.notfound.tip");
        }

        LatestVersionSyncInfo asLatest = value instanceof LatestVersionSyncInfo ? (LatestVersionSyncInfo) value : null;
        ReleaseType type = value.getAvailableVersion().getReleaseType();

        String id;
        String label;

        if (value.hasRemote()) {
            if (asLatest == null) {
                id = value.getID();
                label = "version." + type;
            } else {
                id = asLatest.getVersionID();
                label = "version.latest." + type;
            }

            label = Localizable.nget(label);
            if (type != null) {
                switch (type) {
                    case OLD_BETA:
                        id = id.substring(1);
                        break;
                    case OLD_ALPHA:
                        id = id.startsWith("a") ? id.substring(1) : id;
                        break;
                }
            }
        } else {
            label = null;
            id = value.getID();
        }

        StringBuilder text = new StringBuilder();

        if (label != null) {
            text.append(label).append(' ');
        }

        text.append(id);

        return text.toString();
    }

    public Account.AccountType getShowVersionsFor() {
        return null;
    }
}
