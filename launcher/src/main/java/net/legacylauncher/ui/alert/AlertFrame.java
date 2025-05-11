/*
 * Created by JFormDesigner on Sun Apr 20 20:53:42 YEKT 2025
 */

package net.legacylauncher.ui.alert;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.ui.loc.*;

import net.legacylauncher.ui.support.PreSupportFrame;
import net.legacylauncher.ui.swing.TextPopup;
import net.legacylauncher.ui.swing.editor.*;
import net.legacylauncher.util.U;

/**
 * @author turikhay
 */
public class AlertFrame extends JDialog implements IAlertFrame {
    private static final int WIDTH = 600, INSETS = 25; // also change window size in jfd

    public AlertFrame() {
        super((Frame) null, null, true);
        initComponents();
    }

    private boolean yesNoResult = false;

    @Override
    public void init(String title, String text, String copyableText, int messageType) {
        setupIcon(messageType);
        setupHelpButton(messageType);
        initContent(title, text, copyableText);
        setupYesNoButtons(messageType);
    }

    @Override
    public void showAlert() {
        pack();
        Component relative = null;
        if (LegacyLauncher.getInstance() != null) {
            relative = LegacyLauncher.getInstance().getFrame();
        }
        setLocationRelativeTo(relative);
        //noinspection deprecation
        show();
        dispose();
    }

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
            requestFocusWhenOpened(yesButton);
        } else {
            requestFocusWhenOpened(okButton);
        }
    }

    private void requestFocusWhenOpened(JComponent component) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                component.requestFocusInWindow();
            }
        });
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
                contentPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
                contentPanel.setLayout(new GridBagLayout());
                ((GridBagLayout)contentPanel.getLayout()).columnWidths = new int[] {0, 0, 0};
                ((GridBagLayout)contentPanel.getLayout()).rowHeights = new int[] {0, 0, 0};
                ((GridBagLayout)contentPanel.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
                ((GridBagLayout)contentPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

                //---- iconLabel ----
                iconLabel.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
                contentPanel.add(iconLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                    new Insets(0, 0, 0, 25), 0, 0));

                //---- message ----
                message.setText("<html>\nLorem ipsum dolor sit amet,<br/>consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\n</html>");
                message.setFocusCycleRoot(false);
                message.setFocusable(false);
                contentPanel.add(message, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 0, 0, 0), 0, 0));

                //======== scroll ========
                {
                    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                    scroll.setMinimumSize(null);
                    scroll.setPreferredSize(new Dimension(384, 128));
                    scroll.setRequestFocusEnabled(false);

                    //---- textArea ----
                    textArea.setText("* Try:\n> Run with --stacktrace option to get the stack trace.\n> Run with --info or --debug option to get more log output.\n> Run with --scan to get full insights.");
                    textArea.setEditable(false);
                    textArea.setMinimumSize(null);
                    textArea.setPreferredSize(null);
                    textArea.setRequestFocusEnabled(false);
                    scroll.setViewportView(textArea);
                }
                contentPanel.add(scroll, new GridBagConstraints(0, 1, 2, 1, 0.0, 1.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(25, 0, 0, 0), 0, 0));
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(0, 25, 25, 25));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {70, 0, 70};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0};

                //======== maybeEmptyPanel ========
                {
                    maybeEmptyPanel.setMinimumSize(new Dimension(104, 32));
                    maybeEmptyPanel.setLayout(new CardLayout());

                    //---- helpButton ----
                    helpButton.setText("Need help");
                    helpButton.setMinimumSize(new Dimension(150, 32));
                    helpButton.setPreferredSize(new Dimension(150, 32));
                    helpButton.setMaximumSize(new Dimension(150, 32));
                    maybeEmptyPanel.add(helpButton, "help");

                    //---- versionLabel ----
                    versionLabel.setText("Legacy Launcher");
                    versionLabel.setForeground(SystemColor.inactiveCaption);
                    maybeEmptyPanel.add(versionLabel, "label");
                }
                buttonBar.add(maybeEmptyPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 0, 0, 0), 0, 0));

                //======== buttonPanel ========
                {
                    buttonPanel.setPreferredSize(new Dimension(150, 32));
                    buttonPanel.setMinimumSize(new Dimension(150, 32));
                    buttonPanel.setLayout(new CardLayout());

                    //---- okButton ----
                    okButton.setText("OK");
                    okButton.setPreferredSize(new Dimension(150, 32));
                    buttonPanel.add(okButton, "ok");

                    //======== yesNoPanel ========
                    {
                        yesNoPanel.setLayout(new GridLayout(1, 2, 10, 0));

                        //---- noButton ----
                        noButton.setText("No");
                        noButton.setMaximumSize(new Dimension(150, 32));
                        noButton.setMinimumSize(new Dimension(75, 32));
                        noButton.setPreferredSize(null);
                        yesNoPanel.add(noButton);

                        //---- yesButton ----
                        yesButton.setText("Yes");
                        yesButton.setMaximumSize(new Dimension(150, 32));
                        yesButton.setMinimumSize(new Dimension(75, 32));
                        yesButton.setPreferredSize(null);
                        yesNoPanel.add(yesButton);
                    }
                    buttonPanel.add(yesNoPanel, "yesNo");
                }
                buttonBar.add(buttonPanel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.PAGE_END);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
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
