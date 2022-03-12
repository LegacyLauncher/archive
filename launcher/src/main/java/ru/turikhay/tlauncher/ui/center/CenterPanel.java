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
import ru.turikhay.tlauncher.ui.theme.Theme;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class CenterPanel extends VPanel implements Blockable {
    public static final CenterPanelTheme defaultTheme = new DefaultCenterPanelTheme();
    public static final CenterPanelTheme tipTheme = new TipPanelTheme();
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
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBackground(theme.getPanelBackground());
        messageLabel = new LocalizableLabel("  ");
        messageLabel.setFont(getFont().deriveFont(Font.BOLD));
        messageLabel.setVerticalAlignment(0);
        messageLabel.setHorizontalTextPosition(0);
        messageLabel.setAlignmentX(0.5F);
        messagePanel = new ExtendedPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setAlignmentX(0.5F);
        messagePanel.setInsets(new Insets(3, 0, 3, 0));
        messagePanel.add(messageLabel);
    }

    public void paintComponent(Graphics g0) {
        final double borderSize = Theme.getTheme().getBorderSize();
        final double sf = SwingUtil.getScalingFactor();
        final double step = 1 / sf;

        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(getBackground());
        if (theme.getArc() > 0) {
            g.fillRoundRect(0, 0, getWidth(), getHeight(), theme.getArc(), theme.getArc());
        } else {
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        g.setColor(theme.getBorder());
        for (double xy = step; xy < borderSize; xy += step) {
            drawRect(g, xy);
        }

        if (theme.getShadow() != null) {
            Color shadow = theme.getShadow();
            for (double xy = borderSize; ; xy += step) {
                shadow = U.shiftAlpha(shadow, (int) (-10. / sf));
                if (shadow.getAlpha() <= 0) {
                    break;
                }
                g.setColor(shadow);
                drawRect(g, xy);
            }
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        super.paintComponent(g);
    }

    private void drawRect(Graphics2D g, double xy) {
        double ii = 2 * xy;
        double w = getWidth() - ii;
        double h = getHeight() - ii;
        Shape shape;
        if (theme.getArc() > 0) {
            double arc = theme.getArc() - ii;
            shape = new RoundRectangle2D.Double(xy, xy, w, h, arc, arc);
        } else {
            shape = new Rectangle2D.Double(xy, xy, w, h);
        }
        g.draw(shape);
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
}
