package ru.turikhay.tlauncher.ui.swing;

import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.util.SwingUtil;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.ImageObserver;

public class ImageButton extends ExtendedButton {
    private static final long serialVersionUID = 1L;
    protected Image image;
    protected ImageButton.ImageRotation rotation;
    private int margin;
    private boolean pressed;

    protected ImageButton() {
        rotation = ImageButton.ImageRotation.CENTER;
        margin = 4;
        initListeners();
    }

    public ImageButton(String label, Image image, ImageButton.ImageRotation rotation, int margin) {
        super(label);
        this.rotation = ImageButton.ImageRotation.CENTER;
        this.margin = 4;
        this.image = image;
        this.rotation = rotation;
        this.margin = margin;
        initImage();
        initListeners();
    }

    public ImageButton(String label, Image image, ImageButton.ImageRotation rotation) {
        this(label, image, rotation, 4);
    }

    public ImageButton(String label, Image image) {
        this(label, image, ImageButton.ImageRotation.CENTER);
    }

    public ImageButton(Image image) {
        this(null, image);
    }

    public ImageButton(String imagepath) {
        this(null, loadImage(imagepath));
    }

    public ImageButton(String label, String imagepath, ImageButton.ImageRotation rotation, int margin) {
        this(label, loadImage(imagepath), rotation, margin);
    }

    public ImageButton(String label, String imagepath, ImageButton.ImageRotation rotation) {
        this(label, loadImage(imagepath), rotation);
    }

    public ImageButton(String label, String imagepath) {
        this(label, loadImage(imagepath));
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
        initImage();
        repaint();
    }

    public ImageButton.ImageRotation getRotation() {
        return rotation;
    }

    public int getImageMargin() {
        return margin;
    }

    public void update(Graphics g) {
        super.update(g);
        paint(g);
    }

    public void paint(Graphics g0) {
        super.paint(g0);
        if (image != null) {
            Graphics2D g = (Graphics2D) g0;
            String text = getText();
            boolean drawtext = text != null && text.length() > 0;
            FontMetrics fm = g.getFontMetrics();
            float opacity = isEnabled() ? 1.0F : 0.5F;
            int width = getWidth();
            int height = getHeight();
            int rmargin = margin;
            int offset = pressed ? 1 : 0;
            int iwidth = image.getWidth(null);
            int iheight = image.getHeight(null);
            boolean ix = false;
            int iy = height / 2 - iheight / 2;
            int twidth;
            if (drawtext) {
                twidth = fm.stringWidth(text);
            } else {
                rmargin = 0;
                twidth = 0;
            }

            int ix1;
            switch (rotation) {
                case LEFT:
                    ix1 = width / 2 - twidth / 2 - iwidth - rmargin;
                    break;
                case CENTER:
                    ix1 = width / 2 - iwidth / 2;
                    break;
                case RIGHT:
                    ix1 = width / 2 + twidth / 2 + rmargin;
                    break;
                default:
                    throw new IllegalStateException("Unknown rotation!");
            }

            Composite c = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(3, opacity));
            g.drawImage(image, ix1 + offset, iy + offset, null);
            g.setComposite(c);
            pressed = false;
        }
    }

    protected static Image loadImage(String path) {
        Image image = Images.getImage(path);
        return image.getScaledInstance(SwingUtil.magnify(image.getWidth(null)), SwingUtil.magnify(image.getHeight(null)), 4);
    }

    protected void initImage() {
        if (image != null) {
            setPreferredSize(new Dimension(image.getWidth(null) + 10, image.getHeight(null) + 10));
        }
    }

    private void initListeners() {
        initImage();
        addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
                pressed = true;
            }

            public void mouseReleased(MouseEvent e) {
            }
        });
        addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 32) {
                    pressed = true;
                }
            }

            public void keyReleased(KeyEvent e) {
                pressed = false;
            }

            public void keyTyped(KeyEvent e) {
            }
        });
    }

    public enum ImageRotation {
        LEFT,
        CENTER,
        RIGHT
    }
}
