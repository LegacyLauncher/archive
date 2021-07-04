package ru.turikhay.tlauncher.ui;

import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.U;
import ru.turikhay.util.async.LoopedThread;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;

public class ServicePanel extends ExtendedPanel implements ResizeableComponent {
    private final Image idleImage;
    private final ArrayList<Image> animaImages;
    private Image currentImage;
    private final MainPane pane;
    private int y;
    private float opacity;
    private ServicePanel.ServicePanelThread thread;
    private boolean mouseIn;
    private boolean mouseClicked;
    private boolean shown;
    private long lastCall;

    private static Image load(String url) {
        return Images.getImage(url);
    }

    ServicePanel(MainPane pane) {
        this.pane = pane;
        idleImage = load("heavy-idle.png");
        animaImages = new ArrayList(3);
        animaImages.add(load("heavy-cross0.png"));
        animaImages.add(load("heavy-cross1.png"));
        animaImages.add(load("heavy-raspidoreno.png"));
        pane.add(this);
        opacity = 0.1F;
        y = 0;
        thread = new ServicePanel.ServicePanelThread();
        pane.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                onResize();
            }
        });
        addMouseListenerOriginally(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                mouseClicked = true;
            }

            public void mouseEntered(MouseEvent e) {
                mouseIn = true;
                thread.iterate();
            }

            public void mouseExited(MouseEvent e) {
                mouseClicked = false;
                mouseIn = false;
            }
        });
        set(idleImage);
    }

    private void set(Image img) {
        currentImage = img;
        if (img != null) {
            setSize(img.getWidth(null), img.getHeight(null));
        }

    }

    public void paint(Graphics g0) {
        if (thread.isIterating()) {
            Image image = currentImage;
            if (image != null) {
                Graphics2D g = (Graphics2D) g0;
                g.setComposite(AlphaComposite.getInstance(3, opacity));
                g.drawImage(image, getWidth() / 2 - image.getWidth(null) / 2, getHeight() - y, null);
            }
        }
    }

    public void onResize() {
        setLocation(pane.getWidth() - getWidth(), pane.getHeight() - getHeight());
    }

    class ServicePanelThread extends LoopedThread {
        private static final int PIXEL_STEP = 5;
        private static final int TIMEFRAME = 15;
        private static final float OPACITY_STEP = 0.05F;

        ServicePanelThread() {
            super("ServicePanel");
            startAndWait();
        }

        protected void iterateOnce() {
            if (!shown) {
                int timeout = 5;

                while (true) {
                    --timeout;
                    if (timeout <= 0) {
                        shown = true;
                        y = 1;
                        set(idleImage);

                        while (y > 0) {
                            while (mouseIn) {
                                onIn();
                                if (currentImage == null) {
                                    return;
                                }
                            }

                            while (!mouseIn) {
                                onOut();
                                if (y == 0) {
                                    return;
                                }
                            }
                        }

                        return;
                    }

                    if (!mouseIn) {
                        return;
                    }

                    U.sleepFor(1000L);
                }
            }
        }

        private void onIn() {
            if (y < getHeight()) {
                y = y + 5;
            }

            if (y > getHeight()) {
                y = getHeight();
            }

            if ((double) opacity < 0.9D) {
                opacity = opacity + 0.05F;
            }

            if (opacity > 1.0F) {
                opacity = 1.0F;
            }

            if (y == getHeight() && mouseClicked) {
                Iterator var2 = animaImages.iterator();

                /*try {
                    AudioPlayer.player.start(new AudioStream(Images.getRes("Heavy_yes01.au").openStream()));
                } catch(Exception e) {
                    U.log(e);
                }*/

                while (var2.hasNext()) {
                    BufferedImage frame = (BufferedImage) var2.next();
                    set(frame);
                    repaint();
                    U.sleepFor(100L);
                }

                set(null);
            }

            repaintSleep();
        }

        private void onOut() {
            if (y > 0) {
                y = y - 5;
            }

            if (y < 0) {
                y = 0;
            }

            if ((double) opacity > 0.0D) {
                opacity = opacity - 0.05F;
            }

            if (opacity < 0.0F) {
                opacity = 0.0F;
            }

            repaintSleep();
        }

        private void repaintSleep() {
            repaint();
            U.sleepFor(15L);
        }
    }
}
