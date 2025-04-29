package net.legacylauncher.ui.frames;

import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.loc.LocalizableButton;
import net.legacylauncher.ui.support.PreSupportFrame;
import net.legacylauncher.util.OS;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.async.AsyncThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class InstallOpenGLCompatPackNotice extends VActionFrame {

    private final LocalizableButton installButton;

    public InstallOpenGLCompatPackNotice() {
        super(500);

        setTitlePath("opengl-pack.title");
        Images.getIcon32("download").setup(getHead());
        getHead().setText("opengl-pack.head");
        getBodyText().setText("opengl-pack.body");

        getFooter().setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = -1;

        c.gridx++;
        c.weightx = 0.0;
        LocalizableButton helpButton = new LocalizableButton("loginform.button.support");
        helpButton.setPreferredSize(SwingUtil.magnify(new Dimension(150, 40)));
        helpButton.addActionListener(e -> {
            PreSupportFrame f = new PreSupportFrame();
            f.setLocationRelativeTo(InstallOpenGLCompatPackNotice.this);
            f.setVisible(true);
            dispose();
        });
        getFooter().add(helpButton, c);

        c.gridx++;
        c.weightx = 1.0;
        getFooter().add(Box.createHorizontalBox(), c);

        c.gridx++;
        c.weightx = 0.0;
        installButton = new LocalizableButton("");
        installButton.setPreferredSize(SwingUtil.magnify(new Dimension(150, 40)));
        installButton.addActionListener(e -> {
            OS.openLink("https://apps.microsoft.com/detail/9nqpsl29bfff");
            dispose();
        });
        installButton.setEnabled(false);
        getFooter().add(installButton, c);

        setAlwaysOnTop(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        updateLocale();
        pack();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                installButton.requestFocusInWindow();
                countdown();
            }
        });
    }

    private void scheduleCountdown() {
        AsyncThread.afterSeconds(1, () -> SwingUtil.later(this::countdown));
    }

    private int remaining = 6;

    private void countdown() {
        remaining--;
        if (remaining > 0) {
            installButton.setText("opengl-pack.button.countdown", remaining);
            installButton.setEnabled(false);
            scheduleCountdown();
        } else {
            installButton.setText("opengl-pack.button.install");
            installButton.setEnabled(true);
        }
    }
}
