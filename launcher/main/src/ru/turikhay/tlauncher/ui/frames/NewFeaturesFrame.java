package ru.turikhay.tlauncher.ui.frames;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.*;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NewFeaturesFrame extends VActionFrame {

    final LocalizableCheckbox newNoticeList, promotedServers;
    final LocalizableButton okayButton;

    public NewFeaturesFrame(final TLauncherFrame frame) {
        super(SwingUtil.magnify(500));

        getHead().setIcon(Images.getIcon("plus.png", SwingUtil.magnify(32)));
        getHead().setText("newfeatures.title");

        getBodyText().setText("newfeatures.body");
        getBody().add(newNoticeList = new LocalizableCheckbox("newfeatures.list.new_notice_list"));
        newNoticeList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.mp.defaultScene.setNoticeSidePanelEnabled(newNoticeList.isSelected());
            }
        });
        newNoticeList.setSelected(true);

        getBody().add(promotedServers = new LocalizableCheckbox("newfeatures.list.promoted_servers"));
        promotedServers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.getLauncher().getSettings().set("minecraft.servers.promoted", promotedServers.isSelected());
            }
        });
        promotedServers.setSelected(true);

        getBody().add(Box.createRigidArea(new Dimension(1, SwingUtil.magnify(5))));
        LocalizableHTMLLabel help = new LocalizableHTMLLabel("newfeatures.help");
        help.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        help.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                OS.openLink("http://tlaun.ch/tmp/new_features?locale=" + TLauncher.getInstance().getLang().getLocale().toString());
            }
        });
        getBody().add(help);
        getBody().add(new LocalizableHTMLLabel("newfeatures.bottom"));

        //getFooter().add(new);
        okayButton = new LocalizableButton("newfeatures.button.okay");
        okayButton.setPreferredSize(SwingUtil.magnify(new Dimension(150, 40)));
        okayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        getFooter().setLayout(new BorderLayout());
        getFooter().add(okayButton, "East");

        final ActionListener listener = new ActionListener() {
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
        promotedServers.addActionListener(listener);

        pack();
        okayButton.requestFocusInWindow();
    }
}
