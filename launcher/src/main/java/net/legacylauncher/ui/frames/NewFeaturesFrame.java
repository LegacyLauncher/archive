package net.legacylauncher.ui.frames;

import net.legacylauncher.ui.LegacyLauncherFrame;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.loc.LocalizableButton;
import net.legacylauncher.ui.loc.LocalizableCheckbox;
import net.legacylauncher.ui.loc.LocalizableHTMLLabel;
import net.legacylauncher.util.SwingUtil;

import javax.swing.*;
import java.awt.*;

public class NewFeaturesFrame extends VActionFrame {
    public static final int INCREMENTAL = 1339;

    // NEW FEATURES
    final LocalizableCheckbox oldReleases;
    final LocalizableHTMLLabel oldReleasesDesc;
    //

    final LocalizableButton okayButton;

    public NewFeaturesFrame(final LegacyLauncherFrame frame) {
        super(SwingUtil.magnify(500));

        getHead().setIcon(Images.getIcon32("plus-square"));
        getHead().setText("newfeatures.title");

        getBodyText().setText("newfeatures.body");
        getBody().add(Box.createRigidArea(new Dimension(1, SwingUtil.magnify(10))));

        // NEW FEATURES
        getBody().add(oldReleases = new LocalizableCheckbox("newfeatures.list.old_releases"));
        oldReleases.addActionListener(e -> {
            frame.mp.defaultScene.settingsForm.get().oldVersionsHandler.setValue(oldReleases.isSelected());
            frame.mp.defaultScene.settingsForm.get().saveValues();
        });
        oldReleases.setSelected(frame.mp.defaultScene.settingsForm.get().oldVersionsHandler.getValue().equals("true"));

        oldReleasesDesc = new LocalizableHTMLLabel("newfeatures.list.old_releases.description");
        oldReleasesDesc.setLabelWidth(SwingUtil.magnify(450));
        getBody().add(oldReleasesDesc);
        //

        getBody().add(Box.createRigidArea(new Dimension(1, SwingUtil.magnify(10))));
        getBody().add(new LocalizableHTMLLabel("newfeatures.bottom"));

        //getFooter().add(new);
        okayButton = new LocalizableButton("newfeatures.button.okay");
        okayButton.setPreferredSize(SwingUtil.magnify(new Dimension(150, 40)));
        okayButton.addActionListener(e -> dispose());

        getFooter().setLayout(new BorderLayout());
        getFooter().add(okayButton, "East");

        /*final ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (newNoticeList.isSelected() || promotedServers.isSelected()) {
                    okayButton.setText("newfeatures.button.okay");
                } else {
                    okayButton.setText("newfeatures.button.nope");
                }
            }
        };
        newNoticeList.addActionListener(listener);
        promotedServers.addActionListener(listener);*/

        okayButton.addActionListener(e -> frame.getConfiguration().set("gui.features", NewFeaturesFrame.INCREMENTAL));

        pack();
        okayButton.requestFocusInWindow();
    }
}
