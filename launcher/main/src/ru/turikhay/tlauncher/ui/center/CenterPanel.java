package ru.turikhay.tlauncher.ui.center;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.BlockablePanel;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.Del;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.swing.extended.UnblockablePanel;
import ru.turikhay.tlauncher.ui.swing.extended.VPanel;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

import javax.swing.*;
import java.awt.*;

public class CenterPanel extends VPanel implements Blockable {
    private static final long serialVersionUID = -1975869198322761508L;
    public static final CenterPanelTheme defaultTheme = new DefaultCenterPanelTheme();
    public static final CenterPanelTheme tipTheme = new TipPanelTheme();
    public static final CenterPanelTheme loadingTheme = new LoadingPanelTheme();
    public static final CenterPanelTheme settingsTheme = new SettingsPanelTheme();
    public static final Insets defaultInsets = new MagnifiedInsets(5, 24, 18, 24);
    public static final Insets squareInsets = new MagnifiedInsets(15, 15, 15, 15);
    public static final Insets smallSquareInsets = new MagnifiedInsets(7, 7, 7, 7);
    public static final Insets smallSquareNoTopInsets = new MagnifiedInsets(5, 15, 5, 15);
    public static final Insets noInsets = new MagnifiedInsets(0, 0, 0, 0);
    protected static final int ARC_SIZE = 24;
    private final Insets insets;
    private final CenterPanelTheme theme;
    protected final ExtendedPanel messagePanel;
    protected final LocalizableLabel messageLabel;
    public final TLauncher tlauncher;
    public final Configuration global;
    public final LangConfiguration lang;

    public CenterPanel() {
        this(null, null);
    }

    public CenterPanel(Insets insets) {
        this(null, insets);
    }

    public CenterPanel(CenterPanelTheme theme) {
        this(theme, null);
    }

    public CenterPanel(CenterPanelTheme theme, Insets insets) {
        tlauncher = TLauncher.getInstance();
        global = tlauncher.getSettings();
        lang = tlauncher.getLang();
        this.theme = theme = theme == null ? defaultTheme : theme;
        Insets var10001 = insets == null ? defaultInsets : MagnifiedInsets.get(insets);
        Insets var10002 = insets == null ? defaultInsets : MagnifiedInsets.get(insets);
        this.insets = var10001;
        setLayout(new BoxLayout(this, 3));
        setBackground(theme.getPanelBackground());
        messageLabel = new LocalizableLabel("  ");
        messageLabel.setFont(getFont().deriveFont(1));
        messageLabel.setVerticalAlignment(0);
        messageLabel.setHorizontalTextPosition(0);
        messageLabel.setAlignmentX(0.5F);
        messagePanel = new ExtendedPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, 1));
        messagePanel.setAlignmentX(0.5F);
        messagePanel.setInsets(new Insets(3, 0, 3, 0));
        messagePanel.add(messageLabel);
    }

    public void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;
        byte x = 0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(getBackground());
        //g.fillRect(x, x, getWidth(), getHeight());
        g.fillRoundRect(x, x, getWidth(), getHeight(), 24, 24);
        g.setColor(theme.getBorder());

        int var5;
        for (var5 = 1; var5 < 3; ++var5) {
            //g.drawRect(var5 - 1, var5 - 1, getWidth() - 2 * var5 + 1, getHeight() - 2 * var5 + 1);
            g.drawRoundRect(var5 - 1, var5 - 1, getWidth() - 2 * var5 + 1, getHeight() - 2 * var5 + 1, 24 - 2 * var5 + 1, 24 - 2 * var5 + 1);
        }

        Color shadow = U.shiftAlpha(Color.gray, -155);
        var5 = 3;

        while (true) {
            shadow = U.shiftAlpha(shadow, -10);
            if (shadow.getAlpha() == 0) {
                break;
            }

            g.setColor(shadow);
            //g.drawRect(var5 - 1, var5 - 1, getWidth() - 2 * var5 + 1, getHeight() - 2 * var5 + 1);
            g.drawRoundRect(var5 - 1, var5 - 1, getWidth() - 2 * var5 + 1, getHeight() - 2 * var5 + 1, 24 - 2 * var5 + 1, 24 - 2 * var5 + 1);
            ++var5;
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        super.paintComponent(g);
    }

    public CenterPanelTheme getTheme() {
        return theme;
    }

    public Insets getInsets() {
        return insets;
    }

    protected Del del(int aligment) {
        return new Del(1, aligment, theme.getBorder());
    }

    protected Del del(int aligment, int width, int height) {
        return new Del(1, aligment, width, height, theme.getBorder());
    }

    public void defocus() {
        requestFocusInWindow();
    }

    public boolean setError(String message) {
        messageLabel.setForeground(theme.getFailure());
        messageLabel.setText(message != null && message.length() != 0 ? message : " ");
        return false;
    }

    protected boolean setMessage(String message, Object... vars) {
        messageLabel.setForeground(theme.getFocus());
        messageLabel.setText(message != null && message.length() != 0 ? message : " ", vars);
        return true;
    }

    protected boolean setMessage(String message) {
        return setMessage(message, Localizable.EMPTY_VARS);
    }

    public static BlockablePanel sepPan(final LayoutManager manager, Component... components) {
        BlockablePanel panel = new BlockablePanel(manager) {
            private static final long serialVersionUID = 1L;

            public Insets getInsets() {
                return CenterPanel.noInsets;
            }
        };
        panel.add(components);
        return panel;
    }

    public static BlockablePanel sepPan(Component... components) {
        return sepPan(new GridLayout(0, 1), components);
    }

    public static UnblockablePanel uSepPan(final LayoutManager manager, Component... components) {
        UnblockablePanel panel = new UnblockablePanel(manager) {
            private static final long serialVersionUID = 1L;

            public Insets getInsets() {
                return CenterPanel.noInsets;
            }
        };
        panel.add(components);
        return panel;
    }

    public static UnblockablePanel uSepPan(Component... components) {
        return uSepPan(new GridLayout(0, 1), components);
    }

    public void block(Object reason) {
        setEnabled(false);
        Blocker.blockComponents(this, reason);
    }

    public void unblock(Object reason) {
        setEnabled(true);
        Blocker.unblockComponents(this, reason);
    }

    protected void log(Object... o) {
        U.log("[" + getClass().getSimpleName() + "]", o);
    }
}
