package ru.turikhay.tlauncher.ui.swing;

import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.U;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ImagePanel extends ExtendedPanel {
    private static final long serialVersionUID = 1L;
    public static final float DEFAULT_ACTIVE_OPACITY = 1.0F;
    public static final float DEFAULT_NON_ACTIVE_OPACITY = 0.75F;
    protected final Object animationLock;
    private Image originalImage;
    private Image image;
    private float activeOpacity;
    private float nonActiveOpacity;
    private boolean antiAlias;
    private int timeFrame;
    private float opacity;
    private boolean hover;
    private boolean shown;
    private boolean animating;

    public ImagePanel(String image, float activeOpacity, float nonActiveOpacity, boolean shown, boolean antiAlias) {
        this(Images.getImage(image), activeOpacity, nonActiveOpacity, shown, antiAlias);
    }

    public ImagePanel(String image) {
        this(image, 1.0F, 0.75F, true, true);
    }

    protected ImagePanel(Image image, float activeOpacity, float nonActiveOpacity, boolean shown, boolean antiAlias) {
        animationLock = new Object();
        setImage(image);
        setActiveOpacity(activeOpacity);
        setNonActiveOpacity(nonActiveOpacity);
        setAntiAlias(antiAlias);
        this.shown = shown;
        opacity = shown ? nonActiveOpacity : 0.0F;
        timeFrame = 10;
        setBackground(new Color(0, 0, 0, 0));
        addMouseListenerOriginally(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                onClick();
            }

            public void mouseEntered(MouseEvent e) {
                onMouseEntered();
            }

            public void mouseExited(MouseEvent e) {
                onMouseExited();
            }
        });
    }

    protected void setImage(Image image, boolean resetSize) {
        Object var3 = animationLock;
        synchronized (animationLock) {
            originalImage = image;
            this.image = image;
            if (resetSize && image != null) {
                setSize(image.getWidth(null), image.getHeight(null));
            }

        }
    }

    protected void setImage(Image image) {
        setImage(image, true);
    }

    protected void setActiveOpacity(float opacity) {
        if (opacity <= 1.0F && opacity >= 0.0F) {
            activeOpacity = opacity;
        } else {
            throw new IllegalArgumentException("Invalid opacity! Condition: 0.0F <= opacity (got: " + opacity + ") <= 1.0F");
        }
    }

    protected void setNonActiveOpacity(float opacity) {
        if (opacity <= 1.0F && opacity >= 0.0F) {
            nonActiveOpacity = opacity;
        } else {
            throw new IllegalArgumentException("Invalid opacity! Condition: 0.0F <= opacity (got: " + opacity + ") <= 1.0F");
        }
    }

    protected void setAntiAlias(boolean set) {
        antiAlias = set;
    }

    public void paintComponent(Graphics g0) {
        if (image != null) {
            Graphics2D g = (Graphics2D) g0;
            Composite oldComp = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(3, opacity));
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            g.setComposite(oldComp);
        }
    }

    @Override
    public void show() {
        if (!shown) {
            shown = true;
            Object var1 = animationLock;
            synchronized (animationLock) {
                animating = true;
                setVisible(true);
                opacity = 0.0F;
                float selectedOpacity = hover ? activeOpacity : nonActiveOpacity;

                while (opacity < selectedOpacity) {
                    opacity += 0.01F;
                    if (opacity > selectedOpacity) {
                        opacity = selectedOpacity;
                    }

                    repaint();
                    U.sleepFor((long) timeFrame);
                }

                animating = false;
            }
        }
    }

    public void hide() {
        if (shown) {
            shown = false;
            Object var1 = animationLock;
            synchronized (animationLock) {
                animating = true;

                while (opacity > 0.0F) {
                    opacity -= 0.01F;
                    if (opacity < 0.0F) {
                        opacity = 0.0F;
                    }

                    repaint();
                    U.sleepFor((long) timeFrame);
                }

                setVisible(false);
                animating = false;
            }
        }
    }

    public void setPreferredSize() {
        if (image != null) {
            setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
        }
    }

    protected boolean onClick() {
        return shown;
    }

    protected void onMouseEntered() {
        hover = true;
        if (!animating && shown) {
            opacity = activeOpacity;
            repaint();
        }
    }

    protected void onMouseExited() {
        hover = false;
        if (!animating && shown) {
            opacity = nonActiveOpacity;
            repaint();
        }
    }
}
