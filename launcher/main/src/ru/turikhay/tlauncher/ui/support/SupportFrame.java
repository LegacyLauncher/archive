package ru.turikhay.tlauncher.ui.support;

import ru.turikhay.tlauncher.ui.frames.VActionFrame;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.TextPopup;
import ru.turikhay.tlauncher.ui.text.ExtendedTextField;
import ru.turikhay.util.OS;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class SupportFrame extends VActionFrame {
    protected final String name;

    private final ExtendedTextField textField;

    private final LocalizableButton openButton;
    private ActionListener listener;

    private final String image, url;

    SupportFrame(String name, String image, String url) {
        this.name = StringUtil.requireNotBlank(name, "name");
        this.url = url;

        getFooter().setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new MagnifiedInsets(5, 0, 5, 0);
        c.gridx = 0;
        c.gridy = -1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;

        LocalizableLabel textLabel = new LocalizableLabel("support." + name + ".code");
        ++c.gridy;
        getFooter().add(textLabel, c);

        textField = new ExtendedTextField();
        textField.setFont(textField.getFont().deriveFont(textField.getFont().getSize2D() + 4f));
        textField.setEditable(false);
        textField.addMouseListener(new TextPopup());
        ++c.gridy;
        getFooter().add(textField, c);

        openButton = new LocalizableButton();
        openButton.setPreferredSize(SwingUtil.magnify(new Dimension(1, 40)));
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOpenButtonClicked();
            }
        });
        ++c.gridy;
        getFooter().add(openButton, c);

        setTitlePath("support.title");
        getHead().setText("support.title");
        getHead().setIcon(Images.getIcon(this.image = image, SwingUtil.magnify(32)));
        getBodyText().setText("support." + name + ".body");
        getOpenButton().setText("support." + name + ".open");

        pack();
    }

    SupportFrame(String name, String image) {
        this(name, image, null);
    }

    void setResponse(SendInfoFrame.SendInfoResponse response) {
        U.requireNotNull(response);
        getTextField().setText(getCode(response));
        showAtCenter();
    }

    String getImage() {
        return image;
    }

    ExtendedTextField getTextField() {
        return textField;
    }

    LocalizableButton getOpenButton() {
        return openButton;
    }

    void onOpenButtonClicked() {
        SwingUtil.setClipboard(textField.getValue());
        openUrl();
    }

    boolean isApplicable() {
        return true;
    }

    String getCode(SendInfoFrame.SendInfoResponse response) {
        return response.getPastebinLink();
    }

    public void openUrl() {
        OS.openLink(StringUtil.requireNotBlank(url, "url"));
    }
}
