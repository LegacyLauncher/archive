package net.legacylauncher.ui.support;

import net.legacylauncher.ui.frames.VActionFrame;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.ui.loc.LocalizableButton;
import net.legacylauncher.ui.loc.LocalizableCheckbox;
import net.legacylauncher.ui.swing.extended.ExtendedLabel;
import net.legacylauncher.ui.swing.extended.ExtendedPanel;
import net.legacylauncher.util.OS;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.sysinfo.OSHISystemInfoReporter;
import net.legacylauncher.util.sysinfo.SystemInfoReporter;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PreSupportFrame extends VActionFrame {

    private final ExtendedLabel whatIsDiagnosticLabel = new ExtendedLabel();

    {
        whatIsDiagnosticLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                OS.openLink(Localizable.get("support.pre.diag.url"));
            }
        });
        whatIsDiagnosticLabel.setIcon(Images.getIcon24("lightbulb-o"));
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

    private final SystemInfoReporter systemInfoReporter = OSHISystemInfoReporter.createIfAvailable().orElse(null);
    {
        if (systemInfoReporter != null) {
            systemInfoReporter.queueReport();
        }
    }

    private final SendInfoFrame sendInfoFrame = new SendInfoFrame(systemInfoReporter) {
        @Override
        protected void onSucceeded(Process process, SendInfoResponse result) {
            super.onSucceeded(process, result);
            PreSupportFrame.this.setVisible(false);
        }

        protected void onFailed(Process process, Exception e) {
            super.onFailed(process, e);
            sendDiagnosticCheckbox.setSelected(false);
            PreSupportFrame.this.setVisible(true);
        }
    };

    public PreSupportFrame() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                whatIsDiagnosticLabel.requestFocus();
            }
        });

        setTitlePath("support.pre.title");
        getHead().setIcon(Images.getIcon24("life-ring"));
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

        checkboxPanel.setInsets(0, 0, 10, 0);
        getFooter().add(checkboxPanel, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;

        LocalizableButton continueButton = new LocalizableButton("support.pre.continue");
        continueButton.setPreferredSize(new Dimension(1, SwingUtil.magnify(50)));
        continueButton.addActionListener(e -> onContinued());
        c.gridwidth = GridBagConstraints.REMAINDER;
        getFooter().add(continueButton, c);

        pack();

        whatIsDiagnosticLabel.setToolTipText(Localizable.get("support.pre.diag.whatisit"));
    }

    protected void onContinued() {
        if (sendDiagnosticCheckbox.isSelected()) {
            sendInfoFrame.submit();
        } else {
            new ContactUsFrame().showAtCenter();
        }
        setVisible(false);
    }
}
