package ru.turikhay.tlauncher.ui.frames;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.stats.Stats;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;

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
                TLauncher.kill();
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
