package ru.turikhay.tlauncher.ui;

import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.background.BackgroundManager;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.progress.LaunchProgress;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.tlauncher.ui.scenes.*;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class MainPane extends ExtendedLayeredPane {
    private final TLauncherFrame rootFrame;
    private final boolean repaintEveryTime;
    private PseudoScene scene;
    public final BackgroundManager background;
    public final LaunchProgress progress;
    public final DefaultScene defaultScene;
    public final AccountManagerScene accountManager;
    public final VersionManagerScene versionManager;
    public final NoticeScene noticeScene;
    public final SideNotifier notifier;
    public final RevertFontSize revertFont;
    //final ServicePanel service;

    MainPane(TLauncherFrame frame) {
        super(null);
        rootFrame = frame;
        repaintEveryTime = OS.LINUX.isCurrent();
        log("Creating background...");
        background = new BackgroundManager(this);
        add(background);
        //service = new ServicePanel(this);
        notifier = new SideNotifier();
        notifier.setSize(SwingUtil.magnify(new Dimension(32, 32)));
        add(notifier);
        log("Init Default scene...");
        defaultScene = new DefaultScene(this);
        add(defaultScene);
        log("Init Account manager scene...");
        accountManager = new AccountManagerScene(this);
        add(accountManager);
        log("Init Version manager scene...");
        versionManager = new VersionManagerScene(this);
        add(versionManager);
        noticeScene = new NoticeScene(this);
        add(noticeScene);
        progress = new LaunchProgress(frame);
        frame.getLauncher().getUIListeners().registerMinecraftLauncherListener(progress);
        add(progress);
        revertFont = new RevertFontSize();
        if (revertFont.shouldShow())
            add(revertFont);
        setScene(defaultScene, false);
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                onResize();
            }
        });
    }

    public PseudoScene getScene() {
        return scene;
    }

    public void setScene(PseudoScene scene) {
        setScene(scene, true);
    }

    public void setScene(PseudoScene newscene, boolean animate) {
        if (newscene == null) {
            throw new NullPointerException();
        } else if (!newscene.equals(scene)) {
            Component[] var6;
            int var5 = (var6 = getComponents()).length;

            for (int var4 = 0; var4 < var5; ++var4) {
                Component comp = var6[var4];
                if (!comp.equals(newscene) && comp instanceof PseudoScene) {
                    ((PseudoScene) comp).setShown(false, animate);
                }
            }

            scene = newscene;
            scene.setShown(true);
            if (repaintEveryTime) {
                repaint();
            }

        }
    }

    public void openDefaultScene() {
        setScene(defaultScene);
    }

    public void openAccountEditor() {
        setScene(accountManager);
    }

    public void openVersionManager() {
        setScene(versionManager);
    }

    public void openNoticeScene() {
        setScene(noticeScene);
    }

    public TLauncherFrame getRootFrame() {
        return rootFrame;
    }

    public LaunchProgress getProgress() {
        return progress;
    }

    public void onResize() {
        progress.setBounds(0, getHeight() - ProgressBar.DEFAULT_HEIGHT + 1, getWidth(), ProgressBar.DEFAULT_HEIGHT);
        revertFont.setBounds(0, 0, getWidth(), getFontMetrics(revertFont.revertButton.getFont()).getHeight() * 3);
        //service.onResize();
    }

    public Point getLocationOf(Component comp) {
        Point compLocation = comp.getLocationOnScreen();
        Point paneLocation = getLocationOnScreen();
        return new Point(compLocation.x - paneLocation.x, compLocation.y - paneLocation.y);
    }

    public class RevertFontSize extends ExtendedPanel implements LocalizableComponent {
        private final LocalizableButton revertButton, closeButton;
        private final float oldSize;
        private final int oldSizeInt;

        private RevertFontSize() {
            float size = (float) rootFrame.getConfiguration().getInteger("gui.font.old");

            if (size < TLauncherFrame.minFontSize || size > TLauncherFrame.maxFontSize)
                size = TLauncherFrame.maxFontSize;

            oldSize = size;
            oldSizeInt = (int) size;

            revertButton = new LocalizableButton("revert.font.approve");
            revertButton.setFont(revertButton.getFont().deriveFont(size));
            revertButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    defaultScene.settingsForm.font.setValue(oldSizeInt);
                    defaultScene.settingsForm.saveValues();

                    Alert.showLocMessage("revert.font.approved");

                    closeButton.doClick();
                }
            });

            closeButton = new LocalizableButton("revert.font.close");
            closeButton.setToolTipText("revert.font.close.hint");
            closeButton.setFont(closeButton.getFont().deriveFont(Font.BOLD, size));
            closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    rootFrame.getConfiguration().set("gui.font.old", rootFrame.getConfiguration().getInteger("gui.font"));
                    MainPane.this.remove(RevertFontSize.this);
                    MainPane.this.repaint();
                }
            });

            add(revertButton, closeButton);

            updateLocale();
        }

        public boolean shouldShow() {
            return TLauncherFrame.getFontSize() != oldSize;
        }

        @Override
        public void updateLocale() {
            Localizable.updateContainer(this);
        }
    }

    private void log(String... o) {
        U.log("[MainPane]", o);
    }
}
