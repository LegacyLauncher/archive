package ru.turikhay.tlauncher.ui.progress;

import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.theme.Theme;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Objects;

public class ProgressBar extends JProgressBar {
    private final Color textColor = Theme.getTheme().getForeground();
    public static final int DEFAULT_HEIGHT = SwingUtil.magnify(20);
    private static final int BORDER_SIZE = SwingUtil.magnify(10);
    private static final int EDGE_CHARS = 50;
    private static final int CENTER_CHARS = 30;
    private static final long serialVersionUID = -8095192709934629794L;
    private final Object sync;
    private final Component parent;
    private String wS;
    private String cS;
    private String eS;
    private boolean wS_changed;
    private boolean cS_changed;
    private boolean eS_changed;
    private int wS_x;
    private int cS_x;
    private int eS_x;
    private int oldWidth;

    public ProgressBar(Component parentComp) {
        sync = new Object();
        parent = parentComp;
        if (parent != null) {
            parent.addComponentListener(new ComponentListener() {
                public void componentResized(ComponentEvent e) {
                    updateSize();
                }

                public void componentMoved(ComponentEvent e) {
                }

                public void componentShown(ComponentEvent e) {
                }

                public void componentHidden(ComponentEvent e) {
                }
            });
        }

        Theme.setup(this);
        setFont(getFont().deriveFont(TLauncherFrame.getFontSize()));
        setOpaque(false);
    }

    public ProgressBar() {
        this(null);
    }

    private void updateSize() {
        if (parent != null) {
            setPreferredSize(new Dimension(parent.getWidth(), DEFAULT_HEIGHT));
        }
    }

    public void setStrings(String west, String center, String east, boolean acceptNull, boolean repaint) {
        if (acceptNull || west != null) {
            setWestString(west, false);
        }

        if (acceptNull || center != null) {
            setCenterString(center, false);
        }

        if (acceptNull || east != null) {
            setEastString(east, false);
        }

        if (repaint) {
            repaint();
        }

    }

    public void setStrings(String west, String center, String east) {
        setStrings(west, center, east, true, true);
    }

    public void setWestString(String string, boolean update) {
        string = StringUtil.cut(string, EDGE_CHARS);
        wS_changed = !Objects.equals(wS, string);
        wS = string;
        if (wS_changed && update) {
            repaint();
        }

    }

    public void setWestString(String string) {
        setWestString(string, true);
    }

    public void setCenterString(String string, boolean update) {
        string = StringUtil.cut(string, CENTER_CHARS);
        cS_changed = string == null ? cS == null : !string.equals(cS);
        cS = string;
        if (cS_changed && update) {
            repaint();
        }

    }

    public void setCenterString(String string) {
        setCenterString(string, true);
    }

    public void setEastString(String string, boolean update) {
        string = StringUtil.cut(string, EDGE_CHARS);
        eS_changed = !Objects.equals(eS, string);
        eS = string;
        if (eS_changed && update) {
            repaint();
        }

    }

    public void setEastString(String string) {
        setEastString(string, true);
    }

    public void clearProgress() {
        setIndeterminate(false);
        setValue(0);
        setStrings(null, null, null, true, false);
    }

    public void startProgress() {
        clearProgress();
        updateSize();
        setVisible(true);
    }

    public void stopProgress() {
        setVisible(false);
        clearProgress();
    }

    private void draw(Graphics g) {
        boolean drawWest = wS != null;
        boolean drawCenter = cS != null;
        boolean drawEast = eS != null;
        if (drawWest || drawCenter || drawEast) {
            Font font = g.getFont();
            FontMetrics fm = g.getFontMetrics(font);
            int width = getWidth();
            boolean force = width != oldWidth;
            oldWidth = width;

            if (drawCenter && (force || cS_changed)) {
                cS_x = width / 2 - fm.stringWidth(cS) / 2;
                cS_changed = false;
            }

            if (drawWest && (force || wS_changed)) {
                wS_x = BORDER_SIZE;
                wS_changed = false;
            }

            if (drawEast && (force || eS_changed)) {
                eS_x = width - fm.stringWidth(eS) - BORDER_SIZE;
                eS_changed = false;
            }

            Graphics2D g2D = (Graphics2D) g;
            g.setColor(textColor);
            g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(font);
            drawString(g, wS, wS_x);
            drawString(g, cS, cS_x);
            drawString(g, eS, eS_x);
        }
    }

    private void drawString(Graphics g, String s, int x) {
        if (s != null) {
            int y = (getHeight() - g.getFontMetrics().getDescent() + g.getFontMetrics().getAscent()) / 2;
            g.setColor(Color.white);

            for (int borderX = -1; borderX < 2; ++borderX) {
                for (int borderY = -1; borderY < 2; ++borderY) {
                    g.drawString(s, x + borderX, y + borderY);
                }
            }

            g.setColor(Color.black);
            g.drawString(s, x, y);
        }
    }

    public void update(Graphics g) {
        try {
            super.update(g);
        } catch (Exception var4) {
            return;
        }

        Object e = sync;
        synchronized (sync) {
            draw(g);
        }
    }

    public void paint(Graphics g) {
        try {
            super.paint(g);
        } catch (Exception var4) {
            return;
        }

        Object e = sync;
        synchronized (sync) {
            draw(g);
        }
    }
}
