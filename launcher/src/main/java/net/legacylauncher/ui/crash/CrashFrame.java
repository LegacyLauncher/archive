package net.legacylauncher.ui.crash;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.configuration.Configuration;
import net.legacylauncher.logger.Log4j2ContextHelper;
import net.legacylauncher.minecraft.crash.Crash;
import net.legacylauncher.minecraft.crash.CrashEntry;
import net.legacylauncher.ui.frames.VActionFrame;
import net.legacylauncher.ui.images.ImageIcon;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.loc.LocalizableButton;
import net.legacylauncher.ui.support.PreSupportFrame;
import net.legacylauncher.ui.swing.extended.ExtendedButton;
import net.legacylauncher.ui.swing.extended.ExtendedPanel;
import net.legacylauncher.util.OS;
import net.legacylauncher.util.SwingUtil;
import net.minecraft.launcher.updater.VersionSyncInfo;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.Locale;
import java.util.Objects;

@Slf4j
public final class CrashFrame extends VActionFrame {
    private final ImageIcon crashIcon = Images.getIcon32("bug");

    private final PreSupportFrame supportFrame = new PreSupportFrame() {
        @Override
        protected void onContinued() {
            super.onContinued();
            CrashFrame.this.setVisible(false);
        }
    };

    private Crash crash;

    private final LocalizableButton openLogs = new LocalizableButton("crash.buttons.logs");

    {
        openLogs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File logFile;
                if (crash.getManager().hasProcessLogger()) {
                    logFile = crash.getManager().getProcessLogger().getLogFile().getFile();
                } else {
                    log.warn("Opening logs, but CrashManager had no process logger");
                    logFile = Log4j2ContextHelper.getCurrentLogFile().getFile();
                }
                OS.openFile(logFile);
            }
        });
    }

    private final LocalizableButton askHelp = new LocalizableButton("crash.buttons.support");

    {
        askHelp.addActionListener(e -> supportFrame.showAtCenter());
    }

    private final LocalizableButton exitButton = new LocalizableButton("crash.buttons.exit");

    {
        askHelp.setPreferredSize(SwingUtil.magnify(new Dimension(150, 25)));
        exitButton.setPreferredSize(SwingUtil.magnify(new Dimension(150, 25)));
        exitButton.addActionListener(e -> setVisible(false));
    }

    CrashFrame() {
        getFooter().setLayout(new GridBagLayout());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                if (LegacyLauncher.getInstance().getSettings().getActionOnLaunch() == Configuration.ActionOnLaunch.EXIT) {
                    LegacyLauncher.kill();
                }
            }
        });
    }

    public Crash getCrash() {
        return crash;
    }

    public void setCrash(Crash crash) {
        this.crash = Objects.requireNonNull(crash);

        if (crash.getEntry() == null) {
            initOnUnknown();
        } else {
            if (crash.getEntry().isFake()) {
                return;
            }
            initOnCrash(crash.getEntry());
        }

        pack();
        showAtCenter();
    }

    private void initOnUnknown() {
        log.debug("Unknown crash proceeded");

        setTitlePath("crash.unknown.title");
        getHead().setText("crash.unknown.title");
        getHead().setIcon(crashIcon);
        getBodyText().setText("crash.unknown.body");
        setButtons(true);
    }

    private void initOnCrash(CrashEntry entry) {
        log.debug("Crash entry proceeded: {}", entry);
        setTitlePath(entry.getTitle(), entry.getTitleVars());

        if (entry.getImage() != null) {
            Image image = null;
            try {
                image = SwingUtil.loadImage(entry.getImage());
            } catch (Exception e) {
                log.warn("could not load crash image {}", entry.getImage(), e);
            }
            if (image != null) {
                getHead().setIcon(new javax.swing.ImageIcon(image));
            }
        } else {
            getHead().setIcon(crashIcon);
        }

        getHead().setText(entry.getTitle(), entry.getTitleVars());
        getBodyText().setText(entry.getBody(), entry.getBodyVars());

        ExtendedButton[] graphicsButtons = new ExtendedButton[entry.getButtons().size()];
        for (int i = 0; i < entry.getButtons().size(); i++) {
            graphicsButtons[i] = entry.getButtons().get(i).toGraphicsButton(entry);
        }
        setButtons(entry.isPermitHelp(), graphicsButtons);
    }

    private void setButtons(boolean askHelp, ExtendedButton... buttons) {
        getFooter().removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.insets = SwingUtil.magnify(new Insets(0, 5, 0, 5));
        c.gridx = -1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.BOTH;

        for (ExtendedButton button : buttons) {
            ++c.gridx;
            button.setPreferredSize(new Dimension(button.getMinimumSize().width, SwingUtil.magnify(60)));
            getFooter().add(button, c);
        }

        if (askHelp) {
            if (isProbablyBadVersionCrashed()) {
                log.info("Custom local version is crashed. Disabling help offer.");
            } else {
                ++c.gridx;
                this.askHelp.setPreferredSize(new Dimension(this.askHelp.getMinimumSize().width, SwingUtil.magnify(60)));
                getFooter().add(this.askHelp, c);
            }
        }

        ExtendedPanel buttonPanel = new ExtendedPanel();
        buttonPanel.setLayout(new GridBagLayout());

        GridBagConstraints c0 = new GridBagConstraints();
        c0.insets = SwingUtil.magnify(new Insets(2, 0, 2, 0));
        c0.gridx = 0;
        c0.gridy = -1;
        c0.weightx = 0.0;
        c0.weighty = 1.0;
        c0.anchor = GridBagConstraints.LINE_START;
        c0.fill = GridBagConstraints.BOTH;

        ++c0.gridy;
        buttonPanel.add(openLogs, c0);

        ++c0.gridy;
        buttonPanel.add(exitButton, c0);

        c.weightx = 0.0;
        ++c.gridx;
        getFooter().add(buttonPanel, c);
    }

    private boolean isProbablyBadVersionCrashed() {
        String versionId = crash.getManager().getLauncher().getVersion();

        if (versionId.toLowerCase(Locale.ROOT).contains("forge")
                || versionId.toLowerCase(Locale.ROOT).contains("fabric")) {
            return false; // force or fabric? probably ok
        }
        VersionSyncInfo versionSyncInfo;

        try {
            versionSyncInfo = Objects.requireNonNull(LegacyLauncher.getInstance()
                    .getVersionManager()
                    .getVersionSyncInfo(versionId));
        } catch (RuntimeException rE) {
            log.warn("Couldn't detect if this crash is occurred in the custom version", rE);
            return false; // possible NPEs, fallback to ok
        }

        return !versionSyncInfo.hasRemote(); // local only -> probably not ok
    }
}
