package net.legacylauncher.ui.frames;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.stats.Stats;
import net.legacylauncher.ui.TLauncherFrame;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.loc.LocalizableButton;
import net.legacylauncher.util.OS;
import net.legacylauncher.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FeedbackFrame extends VActionFrame {

    public FeedbackFrame(final TLauncherFrame frame, final String url) {
        super(SwingUtil.magnify(600));

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                frame.dispose();
                LegacyLauncher.kill();
            }
        });

        getHead().setIcon(Images.getIcon32("warning"));
        getHead().setText("feedback.title");

        getBodyText().setText("feedback.body");

        getFooter().setLayout(new BorderLayout());

        LocalizableButton okayButton = new LocalizableButton("feedback.button.okay");
        okayButton.addActionListener(e -> {
            Stats.feedbackStarted();
            OS.openLink(url);
            dispose();
        });
        Images.getIcon24("plus-square").setup(okayButton);
        getFooter().add(okayButton, "Center");

        pack();
        showAtCenter();
    }
}
