package ru.turikhay.tlauncher.ui.support;

import ru.turikhay.tlauncher.ui.frames.VActionFrame;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableCheckbox;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;

import java.awt.*;
import java.awt.event.*;

public class PreSupportFrame extends VActionFrame {
    private final ExtendedLabel whatIsDiagnosticLabel = new ExtendedLabel();

    {
        whatIsDiagnosticLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                OS.openLink(Localizable.get("support.pre.diag.url"));
            }
        });
        whatIsDiagnosticLabel.setIcon(Images.getIcon("lightbulb.png", SwingUtil.magnify(24)));
        whatIsDiagnosticLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private final LocalizableCheckbox sendDiagnosticCheckbox = new LocalizableCheckbox("support.pre.diag.checkbox");

    {
        sendDiagnosticCheckbox.setSelected(true);
    }

    private final ExtendedPanel checkboxPanel = new ExtendedPanel();

    {
        checkboxPanel.setInsets(new Insets(0, 0, 0, 0));
        FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
        layout.setHgap(0);
        layout.setVgap(0);
        checkboxPanel.setLayout(layout);
        checkboxPanel.add(sendDiagnosticCheckbox);
        checkboxPanel.add(whatIsDiagnosticLabel);
    }

    private final SendInfoFrame sendInfoFrame = new SendInfoFrame() {
        @Override
        protected void onSucceeded(Process process, SendInfoResponse result) {
            super.onSucceeded(process, result);
            PreSupportFrame.this.setVisible(false);
        }

        protected void onFailed(Process process, Exception e) {
            super.onFailed(process, e);

            sendDiagnosticCheckbox.setSelected(false);
        }
    };

    private final SupportFrame[] supportFrames = new SupportFrame[]{
            new VkSupportFrame(),
            new FbSupportFrame(),
            new MailSupportFrame()
    };

    private final LocalizableButton[] supportFramesButtons = new LocalizableButton[supportFrames.length];

    {
        for (int i = 0; i < supportFrames.length; i++) {
            final SupportFrame frame = supportFrames[i];

            LocalizableButton button = (supportFramesButtons[i] = new LocalizableButton("support.pre.buttons." + frame.name));
            button.setIcon(Images.getIcon(frame.getImage(), SwingUtil.magnify(24)));
            button.setPreferredSize(new Dimension(1, SwingUtil.magnify(50)));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onSupportFrameSelected(frame);
                }
            });
        }
    }

    public PreSupportFrame() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                whatIsDiagnosticLabel.requestFocus();
            }
        });

        setTitlePath("support.pre.title");
        getHead().setIcon(Images.getIcon("comments-o.png", SwingUtil.magnify(32)));
        getHead().setText("support.pre.title");

        getBodyText().setText("support.pre.body");

        getFooter().setLayout(new GridBagLayout());

        getFooter().removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;

        getFooter().add(checkboxPanel, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;


        for (int i = 0; i < supportFrames.length; i++) {
            if (!supportFrames[i].isApplicable()) {
                continue;
            }
            if(i == supportFrames.length-1) {
                c.gridwidth = GridBagConstraints.REMAINDER;
            }
            getFooter().add(supportFramesButtons[i], c);
            if(++c.gridx % 2 == 0) {
                c.gridx = 0;
                c.gridy++;
            }
        }

        pack();

        whatIsDiagnosticLabel.setToolTipText(Localizable.get("support.pre.diag.whatisit"));
    }

    protected void onSupportFrameSelected(SupportFrame frame) {
        if (sendDiagnosticCheckbox.isSelected()) {
            sendInfoFrame.setFrame(frame);
        } else {
            frame.openUrl();
        }
    }
}
