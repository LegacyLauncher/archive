package net.legacylauncher.ui;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.minecraft.auth.Account;
import net.legacylauncher.minecraft.crash.CrashManager;
import net.legacylauncher.minecraft.launcher.MinecraftException;
import net.legacylauncher.minecraft.launcher.MinecraftExtendedListener;
import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.ui.background.BackgroundManager;
import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.ui.loc.LocalizableButton;
import net.legacylauncher.ui.loc.LocalizableComponent;
import net.legacylauncher.ui.progress.LaunchProgress;
import net.legacylauncher.ui.progress.ProgressBar;
import net.legacylauncher.ui.scenes.*;
import net.legacylauncher.ui.swing.DelayedComponent;
import net.legacylauncher.ui.swing.DelayedComponentLoader;
import net.legacylauncher.ui.swing.extended.ExtendedLayeredPane;
import net.legacylauncher.ui.swing.extended.ExtendedPanel;
import net.legacylauncher.util.OS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

@Slf4j
public class MainPane extends ExtendedLayeredPane {
    private final LegacyLauncherFrame rootFrame;
    private final boolean repaintEveryTime;
    private PseudoScene scene;
    public final BackgroundManager background;
    public final DelayedComponent<LaunchProgress> progress;
    public final DefaultScene defaultScene;
    public final DelayedComponent<AccountManagerScene> accountManager;
    public final DelayedComponent<VersionManagerScene> versionManager;
    public final DelayedComponent<NoticeScene> noticeScene;
    public final DelayedComponent<RevertFontSize> revertFont;

    MainPane(final LegacyLauncherFrame frame) {
        super(null);
        rootFrame = frame;
        repaintEveryTime = OS.LINUX.isCurrent();
        log.trace("Creating background...");
        background = new BackgroundManager(this);
        add(background);
        log.trace("Init Default scene...");
        defaultScene = new DefaultScene(this);
        add(defaultScene);
        log.trace("Init Account manager scene...");
        accountManager = new DelayedComponent<>(new DelayedComponentLoader<AccountManagerScene>() {
            @Override
            public AccountManagerScene loadComponent() {
                return new AccountManagerScene(MainPane.this);
            }

            @Override
            public void onComponentLoaded(AccountManagerScene scene) {
                MainPane.this.add(scene);
                scene.onResize();
                scene.list.updateList();

                Account<?> selected = scene.list.getSelected();
                if (selected != null) {
                    MainPane.this.defaultScene.loginForm.accounts.setAccount(selected);
                }
            }
        });
        //(accountManager);
        log.trace("Init Version manager scene...");
        versionManager = new DelayedComponent<>(new DelayedComponentLoader<VersionManagerScene>() {
            @Override
            public VersionManagerScene loadComponent() {
                return new VersionManagerScene(MainPane.this);
            }

            @Override
            public void onComponentLoaded(VersionManagerScene loaded) {
                MainPane.this.add(loaded);
                LegacyLauncher.getInstance().getVersionManager().asyncRefresh(true);
                loaded.onResize();
            }
        });
        //add(versionManager);
        noticeScene = new DelayedComponent<>(new DelayedComponentLoader<NoticeScene>() {
            @Override
            public NoticeScene loadComponent() {
                return new NoticeScene(MainPane.this);
            }

            @Override
            public void onComponentLoaded(NoticeScene loaded) {
                MainPane.this.add(loaded);
                loaded.onResize();
            }
        });
        //add(noticeScene);
        progress = new DelayedComponent<>(new DelayedComponentLoader<LaunchProgress>() {
            @Override
            public LaunchProgress loadComponent() {
                return new LaunchProgress(frame);
            }

            @Override
            public void onComponentLoaded(LaunchProgress loaded) {
                MainPane.this.add(loaded);
                onResize();
            }
        });
        frame.getLauncher().getUiListeners().registerMinecraftLauncherListener(new MinecraftExtendedListener() {
            @Override
            public void onMinecraftCollecting() {
                progress.get().onMinecraftCollecting();
            }

            @Override
            public void onMinecraftComparingAssets(boolean fast) {
                progress.get().onMinecraftComparingAssets(fast);
            }

            @Override
            public void onMinecraftCheckingJre() {
                progress.get().onMinecraftCheckingJre();
            }

            @Override
            public void onMinecraftMalwareScanning() {
                progress.get().onMinecraftMalwareScanning();
            }

            @Override
            public void onMinecraftDownloading() {
                progress.get().onMinecraftDownloading();
            }

            @Override
            public void onMinecraftReconstructingAssets() {
                progress.get().onMinecraftReconstructingAssets();
            }

            @Override
            public void onMinecraftUnpackingNatives() {
                progress.get().onMinecraftUnpackingNatives();
            }

            @Override
            public void onMinecraftDeletingEntries() {
                progress.get().onMinecraftDeletingEntries();
            }

            @Override
            public void onMinecraftConstructing() {
                progress.get().onMinecraftConstructing();
            }

            @Override
            public void onMinecraftLaunch() {
                progress.get().onMinecraftLaunch();
            }

            @Override
            public void onMinecraftPostLaunch() {
                progress.get().onMinecraftPostLaunch();
            }

            @Override
            public void onMinecraftPrepare() {
                progress.get().onMinecraftPrepare();
            }

            @Override
            public void onMinecraftAbort() {
                progress.get().onMinecraftAbort();
            }

            @Override
            public void onMinecraftClose() {
                progress.get().onMinecraftClose();
            }

            @Override
            public void onMinecraftError(Throwable throwable) {
                progress.get().onMinecraftError(throwable);
            }

            @Override
            public void onMinecraftKnownError(MinecraftException exception) {
                progress.get().onMinecraftKnownError(exception);
            }

            @Override
            public void onCrashManagerInit(CrashManager manager) {
                progress.get().onCrashManagerInit(manager);
            }
        });
        //add(progress);
        revertFont = new DelayedComponent<>(new DelayedComponentLoader<RevertFontSize>() {
            @Override
            public RevertFontSize loadComponent() {
                return new RevertFontSize();
            }

            @Override
            public void onComponentLoaded(RevertFontSize loaded) {
                MainPane.this.add(loaded);
                onResize();
            }
        });

        if (shouldShowRevertFont())
            revertFont.load();

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
        setScene(accountManager.get());
    }

    public void openVersionManager() {
        setScene(versionManager.get());
    }

    public void openNoticeScene() {
        setScene(noticeScene.get());
    }

    public LegacyLauncherFrame getRootFrame() {
        return rootFrame;
    }

    public DelayedComponent<LaunchProgress> getProgress() {
        return progress;
    }

    public void onResize() {
        if (progress.isLoaded()) {
            progress.get().setBounds(0, getHeight() - ProgressBar.DEFAULT_HEIGHT + 1, getWidth(), ProgressBar.DEFAULT_HEIGHT);
        }
        if (revertFont.isLoaded()) {
            revertFont.get().setBounds(0, 0, getWidth(), getFontMetrics(revertFont.get().revertButton.getFont()).getHeight() * 3);
        }
        //service.onResize();
    }

    public Point getLocationOf(Component comp) {
        Point compLocation = comp.getLocationOnScreen();
        Point paneLocation = getLocationOnScreen();
        return new Point(compLocation.x - paneLocation.x, compLocation.y - paneLocation.y);
    }

    private boolean shouldShowRevertFont() {
        float size = (float) rootFrame.getConfiguration().getInteger("gui.font.old");
        if (size < LegacyLauncherFrame.minFontSize || size > LegacyLauncherFrame.maxFontSize)
            size = LegacyLauncherFrame.maxFontSize;
        float oldSize = size;
        return LegacyLauncherFrame.getFontSize() != oldSize;
    }

    public class RevertFontSize extends ExtendedPanel implements LocalizableComponent {
        private final LocalizableButton revertButton, closeButton;
        private final int oldSizeInt;

        private RevertFontSize() {
            float size = (float) rootFrame.getConfiguration().getInteger("gui.font.old");

            if (size < LegacyLauncherFrame.minFontSize || size > LegacyLauncherFrame.maxFontSize)
                size = LegacyLauncherFrame.maxFontSize;

            float oldSize = size;
            oldSizeInt = (int) size;

            revertButton = new LocalizableButton("revert.font.approve");
            revertButton.setFont(revertButton.getFont().deriveFont(size));
            revertButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    rootFrame.getConfiguration().set("gui.font", rootFrame.getConfiguration().getInteger("gui.font.old"), false);

                    Alert.showLocMessage("revert.font.approved");

                    closeButton.doClick();
                }
            });

            closeButton = new LocalizableButton("revert.font.close");
            closeButton.setToolTipText("revert.font.close.hint");
            closeButton.setFont(closeButton.getFont().deriveFont(Font.BOLD, size));
            closeButton.addActionListener(e -> {
                rootFrame.getConfiguration().set("gui.font.old", rootFrame.getConfiguration().getInteger("gui.font"));
                MainPane.this.remove(RevertFontSize.this);
                MainPane.this.repaint();
            });

            add(new JComponent[]{revertButton, closeButton});

            updateLocale();
        }

        @Override
        public void updateLocale() {
            Localizable.updateContainer(this);
        }
    }
}
