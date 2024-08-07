package net.legacylauncher.ui.login.buttons;

import net.legacylauncher.ui.block.Blockable;
import net.legacylauncher.ui.block.Blocker;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.loc.LocalizableButton;
import net.legacylauncher.ui.loc.LocalizableMenuItem;
import net.legacylauncher.ui.login.LoginForm;
import net.legacylauncher.ui.notice.Notice;
import net.legacylauncher.ui.notice.NoticeManagerListener;
import net.legacylauncher.ui.scenes.DefaultScene;
import net.legacylauncher.util.SwingUtil;

import javax.swing.*;
import java.awt.*;

import static net.legacylauncher.util.SwingUtil.updateUINullable;

public class SettingsButton extends LocalizableButton implements Blockable, NoticeManagerListener {
    private final LoginForm lf;
    private final JPopupMenu popup;
    private final LocalizableMenuItem accountManager;
    private final LocalizableMenuItem versionManager;
    private final LocalizableMenuItem notices;

    SettingsButton(LoginForm loginform) {
        lf = loginform;
        setToolTipText("loginform.button.settings");
        setIcon(Images.getIcon24("bars"));
        popup = new JPopupMenu();
        LocalizableMenuItem settings = new LocalizableMenuItem("loginform.button.settings.launcher");
        settings.addActionListener(e -> lf.scene.setSidePanel(DefaultScene.SidePanel.SETTINGS));
        popup.add(settings);
        versionManager = new LocalizableMenuItem("loginform.button.settings.version");
        versionManager.addActionListener(e -> lf.pane.openVersionManager());
        popup.add(versionManager);
        accountManager = new LocalizableMenuItem("loginform.button.settings.account");
        accountManager.addActionListener(e -> lf.pane.openAccountEditor());
        popup.add(accountManager);
        notices = LocalizableMenuItem.newItem("loginform.button.settings.notices", e -> {
            //lf.scene.getMainPane().openNoticeScene();
            lf.scene.setNoticeSidePanelEnabled(true);
            lf.scene.setSidePanel(DefaultScene.SidePanel.NOTICES);
        });
        updateNoticeEntry();
        setPreferredSize(new Dimension(30, getHeight()));
        addActionListener(e -> callPopup());
        lf.scene.getMainPane().getRootFrame().getNotices().addListener(this, true);
    }

    public Insets getInsets() {
        return SwingUtil.magnify(super.getInsets());
    }

    void callPopup() {
        lf.defocus();
        popup.show(this, 0, getHeight());
    }

    public void block(Object reason) {
        if (reason.equals("auth") || reason.equals("launch")) {
            Blocker.blockComponents(reason, accountManager, versionManager);
        }

    }

    public void unblock(Object reason) {
        Blocker.unblockComponents(reason, accountManager, versionManager);
    }

    @Override
    public void onNoticeSelected(Notice notice) {
        /*if(notice == null  && lf.scene.getMainPane().getRootFrame().getNotices().getForCurrentLocale() != null) {
            popup.add(notices);
        } else {
            popup.remove(notices);
        }*/
    }

    @Override
    public void onNoticePromoted(Notice promotedNotice) {

    }

    @Override
    public void updateLocale() {
        super.updateLocale();
        updateNoticeEntry();
    }

    @Override
    public void updateUI() {
        updateUINullable(popup);
        super.updateUI();
    }

    private void updateNoticeEntry() {
        if (lf.scene.getMainPane().getRootFrame().getNotices().getForCurrentLocale() == null) {
            popup.remove(notices);
        } else {
            popup.add(notices);
        }
    }
}
