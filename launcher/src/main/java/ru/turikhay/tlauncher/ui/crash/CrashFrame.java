package ru.turikhay.tlauncher.ui.crash;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.crash.Crash;
import ru.turikhay.tlauncher.minecraft.crash.CrashEntry;
import ru.turikhay.tlauncher.ui.frames.VActionFrame;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.support.PreSupportFrame;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class CrashFrame extends VActionFrame {
    private static final Logger LOGGER = LogManager.getLogger(CrashFrame.class);

    private final ImageIcon crashIcon = Images.getIcon("bug.png");

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
                OS.openFile(crash.getManager().getProcessLogger().getLogFile().getFile());
            }
        });
    }

    private final LocalizableButton askHelp = new LocalizableButton("crash.buttons.support");

    {
        askHelp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                supportFrame.showAtCenter();
            }
        });
    }

    private final LocalizableButton exitButton = new LocalizableButton("crash.buttons.exit");
    {
        askHelp.setPreferredSize(SwingUtil.magnify(new Dimension(150, 25)));
        exitButton.setPreferredSize(SwingUtil.magnify(new Dimension(150, 25)));
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
    }

    CrashFrame(CrashProcessingFrame frame) {
        getFooter().setLayout(new GridBagLayout());
    }

    public Crash getCrash() {
        return crash;
    }

    public void setCrash(Crash crash) {
        this.crash = U.requireNotNull(crash);

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
        LOGGER.debug("Unknown crash proceeded");

        setTitlePath("crash.unknown.title");
        getHead().setText("crash.unknown.title");
        getHead().setIcon(Images.getIcon("bug.png", 32));
        getBodyText().setText("crash.unknown.body");
        setButtons(true);
    }

    private void initOnCrash(CrashEntry entry) {
        LOGGER.debug("Crash entry proceeded: {}", entry);
        setTitlePath(entry.getTitle(), entry.getTitleVars());

        if (entry.getImage() != null) {
            loadImage:
            {
                Image image;
                try {
                    image = SwingUtil.loadImage(entry.getImage());
                } catch (Exception e) {
                    LOGGER.warn("could not load crash image {}", entry.getImage(), e);
                    break loadImage;
                }
                getHead().setIcon(new ImageIcon(image, SwingUtil.magnify(32), false));
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

        if(askHelp) {
            ++c.gridx;
            this.askHelp.setPreferredSize(new Dimension(this.askHelp.getMinimumSize().width, SwingUtil.magnify(60)));
            getFooter().add(this.askHelp, c);
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
}
