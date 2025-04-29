/*
 * Created by JFormDesigner on Sun Apr 20 20:53:42 YEKT 2025
 */

package net.legacylauncher.ui.alert;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Locale;
import javax.swing.*;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.ui.loc.*;

import net.legacylauncher.ui.support.PreSupportFrame;
import net.legacylauncher.ui.swing.TextPopup;
import net.legacylauncher.ui.swing.editor.*;
import net.legacylauncher.util.U;
import net.miginfocom.swing.*;

/**
 * @author turikhay
 */
public class AlertFrame extends JDialog {
    private static final int WIDTH = 600, INSETS = 25; // also change window size in jfd

    public AlertFrame(String title, String text, String copyableText, int messageType) {
        super((Frame) null, null, true);
        initComponents();
        setupIcon(messageType);
        setupHelpButton(messageType);
        initContent(title, text, copyableText);
        setupYesNoButtons(messageType);
    }

    private boolean yesNoResult = false;

    public boolean isYes() {
        return yesNoResult;
    }

    private void setupYesNoButtons(int messageType) {
        if (messageType == JOptionPane.QUESTION_MESSAGE) {
            ((CardLayout) buttonPanel.getLayout()).show(buttonPanel, "yesNo");
            yesButton.setText("ui.yes");
            yesButton.addActionListener(yesNoListener(true));
            noButton.setText("ui.no");
            noButton.addActionListener(yesNoListener(false));
            yesButton.requestFocusInWindow();
        } else {
            okButton.requestFocusInWindow();
        }
    }

    private ActionListener yesNoListener(boolean isYes) {
        return e -> {
            AlertFrame.this.yesNoResult = isYes;
            AlertFrame.this.setVisible(false);
        };
    }

    private void setupHelpButton(int messageType) {
        if (messageType == JOptionPane.INFORMATION_MESSAGE || messageType == JOptionPane.QUESTION_MESSAGE) {
            ((CardLayout) maybeEmptyPanel.getLayout()).show(maybeEmptyPanel, "label");
            versionLabel.setText("Legacy Launcher " + U.getMinorVersion(LegacyLauncher.getVersion()));
        }
    }

    private void onHelpRequested() {
        PreSupportFrame frame = new PreSupportFrame();
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
        setVisible(false);
    }

    private void setupIcon(int messageType) {
        String iconPath = null;
        switch (messageType) {
            case JOptionPane.INFORMATION_MESSAGE:
                iconPath = "OptionPane.informationIcon";
                break;
            case JOptionPane.WARNING_MESSAGE:
                iconPath = "OptionPane.warningIcon";
                break;
            case JOptionPane.ERROR_MESSAGE:
                iconPath = "OptionPane.errorIcon";
                break;
            case JOptionPane.QUESTION_MESSAGE:
                iconPath = "OptionPane.questionIcon";
                break;
        }
        if (iconPath != null) {
            iconLabel.setIcon(UIManager.getIcon(iconPath));
        }
    }

    private void initContent(String title, String text, String copyableText) {
        setTitle(title);
        message.setContentType("text/html");
        int globWidth = getMinimumSize().width;
        int width = globWidth - INSETS * 3 - iconLabel.getIcon().getIconWidth();
        message.setText(String.format(Locale.ROOT,
                        "<div width=%d>%s</div>", width, text.replace("\n", "<br/>"))
        );
        if (copyableText == null) {
            scroll.setVisible(false);
        } else {
            int taWidth = globWidth - INSETS * 2;
            textArea.addMouseListener(new TextPopup());
            textArea.setText(copyableText);
            textArea.setCaretPosition(0);
            Insets insets = textArea.getInsets();
            FontMetrics fontMetrics = textArea.getFontMetrics(textArea.getFont());
            insets.bottom += fontMetrics.getHeight() * 2;
            Dimension prefSize = AlertPanel.getPrefSize(copyableText, taWidth, 150, fontMetrics, insets);
            scroll.setPreferredSize(prefSize);
            scroll.setMaximumSize(prefSize);
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        iconLabel = new JLabel();
        message = new EditorPane();
        scroll = new JScrollPane();
        textArea = new JTextArea();
        buttonBar = new JPanel();
        maybeEmptyPanel = new JPanel();
        helpButton = new LocalizableButton();
        versionLabel = new JLabel();
        buttonPanel = new JPanel();
        okButton = new LocalizableButton();
        yesNoPanel = new JPanel();
        noButton = new LocalizableButton();
        yesButton = new LocalizableButton();

        //======== this ========
        setMinimumSize(new Dimension(600, 28));
        setMaximumSize(new Dimension(600, 600));
        setAlwaysOnTop(true);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new MigLayout(
                    "fillx,insets 25,hidemode 2,aligny top,gap 0 0",
                    // columns
                    "[]" +
                    "[fill,grow]",
                    // rows
                    "[]" +
                    "[]"));

                //---- iconLabel ----
                iconLabel.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
                contentPanel.add(iconLabel, "aligny top,growy 0,gapx null 25");

                //---- message ----
                message.setText("<html>\nLorem ipsum dolor sit amet,<br/>consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\n</html>");
                message.setFocusCycleRoot(false);
                contentPanel.add(message, "aligny top,grow 100 0");

                //======== scroll ========
                {

                    //---- textArea ----
                    textArea.setText("* Try:\n> Run with --stacktrace option to get the stack trace.\n> Run with --info or --debug option to get more log output.\n> Run with --scan to get full insights.");
                    textArea.setEditable(false);
                    scroll.setViewportView(textArea);
                }
                contentPanel.add(scroll, "pad 25 0 0 0,cell 0 1 2 1,grow");
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setLayout(new MigLayout(
                    "fillx,insets dialog",
                    // columns
                    "[button,fill]" +
                    "[fill,grow]" +
                    "[button,fill]",
                    // rows
                    "[]"));

                //======== maybeEmptyPanel ========
                {
                    maybeEmptyPanel.setLayout(new CardLayout());

                    //---- helpButton ----
                    helpButton.setText("Need help");
                    maybeEmptyPanel.add(helpButton, "help");

                    //---- versionLabel ----
                    versionLabel.setText("Legacy Launcher");
                    versionLabel.setForeground(SystemColor.inactiveCaption);
                    maybeEmptyPanel.add(versionLabel, "label");
                }
                buttonBar.add(maybeEmptyPanel, "cell 0 0");

                //======== buttonPanel ========
                {
                    buttonPanel.setLayout(new CardLayout());

                    //---- okButton ----
                    okButton.setText("OK");
                    okButton.setNextFocusableComponent(helpButton);
                    buttonPanel.add(okButton, "ok");

                    //======== yesNoPanel ========
                    {
                        yesNoPanel.setLayout(new MigLayout(
                            "insets 0,hidemode 3",
                            // columns
                            "[fill]" +
                            "[fill]",
                            // rows
                            "[]"));

                        //---- noButton ----
                        noButton.setNextFocusableComponent(helpButton);
                        yesNoPanel.add(noButton, "cell 0 0");

                        //---- yesButton ----
                        yesButton.setNextFocusableComponent(helpButton);
                        yesNoPanel.add(yesButton, "cell 1 0");
                    }
                    buttonPanel.add(yesNoPanel, "yesNo");
                }
                buttonBar.add(buttonPanel, "cell 2 0,grow");
            }
            dialogPane.add(buttonBar, BorderLayout.PAGE_END);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
        okButton.addActionListener(e -> AlertFrame.this.setVisible(false));
        helpButton.addActionListener(e -> onHelpRequested());
        helpButton.setText("loginform.button.support");
        okButton.setText("ui.ok");
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel iconLabel;
    private EditorPane message;
    private JScrollPane scroll;
    private JTextArea textArea;
    private JPanel buttonBar;
    private JPanel maybeEmptyPanel;
    private LocalizableButton helpButton;
    private JLabel versionLabel;
    private JPanel buttonPanel;
    private LocalizableButton okButton;
    private JPanel yesNoPanel;
    private LocalizableButton noButton;
    private LocalizableButton yesButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
