package net.legacylauncher.ui.notice;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.ui.loc.LocalizableMenuItem;
import net.legacylauncher.ui.login.LoginForm;
import net.legacylauncher.util.OS;
import net.minecraft.launcher.updater.VersionSyncInfo;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class LauncherNoticeAction extends NoticeAction {
    private final String launcherName, downloadUrl;

    LauncherNoticeAction(String launcherName, String downloadUrl) {
        super("launcher");
        this.launcherName = launcherName;
        this.downloadUrl = downloadUrl;
    }

    @Override
    List<? extends JMenuItem> getMenuItemList() {
        List<LocalizableMenuItem> list = new ArrayList<>();
        LocalizableMenuItem launchItem = new LocalizableMenuItem(L10N_PREFIX + "start", launcherName);
        launchItem.addActionListener(e -> startLauncher());
        list.add(launchItem);
        if (downloadUrl != null) {
            LocalizableMenuItem downloadItem = new LocalizableMenuItem(L10N_PREFIX + "download");
            downloadItem.addActionListener(e -> OS.openLink(downloadUrl));
            list.add(downloadItem);
        }
        return list;
    }

    private void startLauncher() {
        VersionSyncInfo vs = LegacyLauncher.getInstance().getVersionManager().getVersionSyncInfo(launcherName);
        if (vs == null) {
            if (downloadUrl == null) {
                Alert.showLocError(L10N_PREFIX + "start.error.not-found");
            } else {
                if (Alert.showLocQuestion(L10N_PREFIX + "start.error.not-found.download")) {
                    OS.openLink(downloadUrl);
                }
            }
            return;
        }
        LoginForm lf = LegacyLauncher.getInstance().getFrame().mp.defaultScene.loginForm;
        lf.startLauncher(vs, null, 0);
    }
}
