package ru.turikhay.tlauncher.ui.support;

import ru.turikhay.tlauncher.ui.frames.VActionFrame;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.TextPopup;
import ru.turikhay.tlauncher.ui.text.ExtendedTextField;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;

public class OpenLinkFrame extends VActionFrame {

    public OpenLinkFrame(final String link) {
        setTitlePath("support.open_link.title");
        getHead().setIcon(Images.getIcon32("check-square"));
        getHead().setText("support.open_link.title");
        getBodyText().setText("support.open_link.body");

        getFooter().removeAll();
        getFooter().setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new MagnifiedInsets(5, 0, 5, 0);
        c.gridx = 0;
        c.gridy = -1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;

        ExtendedTextField textField = new ExtendedTextField();
        textField.setFont(textField.getFont().deriveFont(textField.getFont().getSize2D() + 4f));
        textField.setEditable(false);
        textField.setText(link);
        textField.addMouseListener(new TextPopup());
        ++c.gridy;
        getFooter().add(textField, c);

        LocalizableButton goButton = new LocalizableButton("support.open_link.go");
        goButton.setPreferredSize(SwingUtil.magnify(new Dimension(1, 40)));
        goButton.addActionListener(e -> {
            OS.openLink(link);
            OpenLinkFrame.this.dispose();
        });
        ++c.gridy;
        getFooter().add(goButton, c);

        pack();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }
}
