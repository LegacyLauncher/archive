package ru.turikhay.tlauncher.ui.notice;

import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.util.OS;

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
        VersionSyncInfo vs = TLauncher.getInstance().getVersionManager().getVersionSyncInfo(launcherName);
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
        LoginForm lf = TLauncher.getInstance().getFrame().mp.defaultScene.loginForm;
        lf.startLauncher(vs, null, 0);
    }
}
