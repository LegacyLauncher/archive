package ru.turikhay.tlauncher.ui.swing.editor;

import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import javax.swing.text.Position.Bias;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.InlineView;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.image.ImageObserver;

public class ExtendedImageView extends View {
    private static boolean sIsInc = false;
    private static int sIncRate = 100;
    private static final String PENDING_IMAGE = "html.pendingImage";
    private static final String MISSING_IMAGE = "html.missingImage";
    private static final String IMAGE_CACHE_PROPERTY = "imageCache";
    private static final int DEFAULT_WIDTH = 38;
    private static final int DEFAULT_HEIGHT = 38;
    private static final int LOADING_FLAG = 1;
    private static final int LINK_FLAG = 2;
    private static final int WIDTH_FLAG = 4;
    private static final int HEIGHT_FLAG = 8;
    private static final int RELOAD_FLAG = 16;
    private static final int RELOAD_IMAGE_FLAG = 32;
    private static final int SYNC_LOAD_FLAG = 64;
    private AttributeSet attr;
    private Image image;
    private Image disabledImage;
    private int width;
    private int height;
    private int state = 48;
    private Container container;
    private Rectangle fBounds = new Rectangle();
    private Color borderColor;
    private short borderSize;
    private short leftInset;
    private short rightInset;
    private short topInset;
    private short bottomInset;
    private ImageObserver imageObserver = new ExtendedImageView.ImageHandler();
    private View altView;
    private float vAlign;

    public ExtendedImageView(Element elem) {
        super(elem);
    }

    public String getAltText() {
        return (String) getElement().getAttributes().getAttribute(Attribute.ALT);
    }

    public String getImageSource() {
        return (String) getElement().getAttributes().getAttribute(Attribute.SRC);
    }

    public Icon getNoImageIcon() {
        return (Icon) UIManager.getLookAndFeelDefaults().get("html.missingImage");
    }

    public Icon getLoadingImageIcon() {
        return (Icon) UIManager.getLookAndFeelDefaults().get("html.pendingImage");
    }

    public Image getImage() {
        sync();
        return image;
    }

    private Image getImage(boolean enabled) {
        Image img = getImage();
        if (!enabled) {
            if (disabledImage == null) {
                disabledImage = GrayFilter.createDisabledImage(img);
            }

            img = disabledImage;
        }

        return img;
    }

    public void setLoadsSynchronously(boolean newValue) {
        synchronized (this) {
            if (newValue) {
                state |= 64;
            } else {
                state = (state | 64) ^ 64;
            }

        }
    }

    public boolean getLoadsSynchronously() {
        return (state & 64) != 0;
    }

    protected StyleSheet getStyleSheet() {
        HTMLDocument doc = (HTMLDocument) getDocument();
        return doc.getStyleSheet();
    }

    public AttributeSet getAttributes() {
        sync();
        return attr;
    }

    public String getToolTipText(float x, float y, Shape allocation) {
        return getAltText();
    }

    protected void setPropertiesFromAttributes() {
        StyleSheet sheet = getStyleSheet();
        attr = sheet.getViewAttributes(this);
        borderSize = (short) getIntAttr(Attribute.BORDER, 0);
        leftInset = rightInset = (short) (getIntAttr(Attribute.HSPACE, 0) + borderSize);
        topInset = bottomInset = (short) (getIntAttr(Attribute.VSPACE, 0) + borderSize);
        borderColor = ((StyledDocument) getDocument()).getForeground(getAttributes());
        AttributeSet attr = getElement().getAttributes();
        Object alignment = attr.getAttribute(Attribute.ALIGN);
        vAlign = 1.0F;
        if (alignment != null) {
            String alignment1 = alignment.toString();
            if ("top".equals(alignment1)) {
                vAlign = 0.0F;
            } else if ("middle".equals(alignment1)) {
                vAlign = 0.5F;
            }
        }

        AttributeSet anchorAttr = (AttributeSet) attr.getAttribute(Tag.A);
        if (anchorAttr != null && anchorAttr.isDefined(Attribute.HREF)) {
            synchronized (this) {
                state |= 2;
            }
        } else {
            synchronized (this) {
                state = (state | 2) ^ 2;
            }
        }

    }

    public void setParent(View parent) {
        View oldParent = getParent();
        super.setParent(parent);
        container = parent != null ? getContainer() : null;
        if (oldParent != parent) {
            synchronized (this) {
                state |= 16;
            }
        }

    }

    public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
        super.changedUpdate(e, a, f);
        synchronized (this) {
            state |= 48;
        }

        preferenceChanged(null, true, true);
    }

    public void paint(Graphics g, Shape a) {
        sync();
        Rectangle rect = a instanceof Rectangle ? (Rectangle) a : a.getBounds();
        Rectangle clip = g.getClipBounds();
        fBounds.setBounds(rect);
        paintHighlights(g, a);
        paintBorder(g, rect);
        if (clip != null) {
            g.clipRect(rect.x + leftInset, rect.y + topInset, rect.width - leftInset - rightInset, rect.height - topInset - bottomInset);
        }

        Container host = getContainer();
        Image img = getImage(host == null || host.isEnabled());
        Icon icon;
        if (img != null) {
            if (!hasPixels(img)) {
                icon = getLoadingImageIcon();
                if (icon != null) {
                    icon.paintIcon(host, g, rect.x + leftInset, rect.y + topInset);
                }
            } else {
                g.drawImage(img, rect.x + leftInset, rect.y + topInset, width, height, imageObserver);
            }
        } else {
            icon = getNoImageIcon();
            if (icon != null) {
                icon.paintIcon(host, g, rect.x + leftInset, rect.y + topInset);
            }

            View view = getAltView();
            if (view != null && ((state & 4) == 0 || width > 38)) {
                Rectangle altRect = new Rectangle(rect.x + leftInset + 38, rect.y + topInset, rect.width - leftInset - rightInset - 38, rect.height - topInset - bottomInset);
                view.paint(g, altRect);
            }
        }

        if (clip != null) {
            g.setClip(clip.x, clip.y, clip.width, clip.height);
        }

    }

    private void paintHighlights(Graphics g, Shape shape) {
        if (container instanceof JTextComponent) {
            JTextComponent tc = (JTextComponent) container;
            Highlighter h = tc.getHighlighter();
            if (h instanceof LayeredHighlighter) {
                ((LayeredHighlighter) h).paintLayeredHighlights(g, getStartOffset(), getEndOffset(), shape, tc, this);
            }
        }

    }

    private void paintBorder(Graphics g, Rectangle rect) {
        Color color = borderColor;
        if ((borderSize > 0 || image == null) && color != null) {
            int xOffset = leftInset - borderSize;
            int yOffset = topInset - borderSize;
            g.setColor(color);
            short n = image == null ? 1 : borderSize;

            for (int counter = 0; counter < n; ++counter) {
                g.drawRect(rect.x + xOffset + counter, rect.y + yOffset + counter, rect.width - counter - counter - xOffset - xOffset - 1, rect.height - counter - counter - yOffset - yOffset - 1);
            }
        }

    }

    public float getPreferredSpan(int axis) {
        sync();
        if (axis == 0 && (state & 4) == 4) {
            getPreferredSpanFromAltView(axis);
            return (float) (width + leftInset + rightInset);
        } else if (axis == 1 && (state & 8) == 8) {
            getPreferredSpanFromAltView(axis);
            return (float) (height + topInset + bottomInset);
        } else {
            Image image = getImage();
            if (image != null) {
                switch (axis) {
                    case 0:
                        return (float) (width + leftInset + rightInset);
                    case 1:
                        return (float) (height + topInset + bottomInset);
                    default:
                        throw new IllegalArgumentException("Invalid axis: " + axis);
                }
            } else {
                View view = getAltView();
                float retValue = 0.0F;
                if (view != null) {
                    retValue = view.getPreferredSpan(axis);
                }

                switch (axis) {
                    case 0:
                        return retValue + (float) (width + leftInset + rightInset);
                    case 1:
                        return retValue + (float) (height + topInset + bottomInset);
                    default:
                        throw new IllegalArgumentException("Invalid axis: " + axis);
                }
            }
        }
    }

    public float getAlignment(int axis) {
        switch (axis) {
            case 1:
                return vAlign;
            default:
                return super.getAlignment(axis);
        }
    }

    public Shape modelToView(int pos, Shape a, Bias b) throws BadLocationException {
        int p0 = getStartOffset();
        int p1 = getEndOffset();
        if (pos >= p0 && pos <= p1) {
            Rectangle r = a.getBounds();
            if (pos == p1) {
                r.x += r.width;
            }

            r.width = 0;
            return r;
        } else {
            return null;
        }
    }

    public int viewToModel(float x, float y, Shape a, Bias[] bias) {
        Rectangle alloc = (Rectangle) a;
        if (x < (float) (alloc.x + alloc.width)) {
            bias[0] = Bias.Forward;
            return getStartOffset();
        } else {
            bias[0] = Bias.Backward;
            return getEndOffset();
        }
    }

    public void setSize(float width, float height) {
        sync();
        if (getImage() == null) {
            View view = getAltView();
            if (view != null) {
                view.setSize(Math.max(0.0F, width - (float) (38 + leftInset + rightInset)), Math.max(0.0F, height - (float) (topInset + bottomInset)));
            }
        }

    }

    private boolean hasPixels(Image image) {
        return image != null && image.getHeight(imageObserver) > 0 && image.getWidth(imageObserver) > 0;
    }

    private float getPreferredSpanFromAltView(int axis) {
        if (getImage() == null) {
            View view = getAltView();
            if (view != null) {
                return view.getPreferredSpan(axis);
            }
        }

        return 0.0F;
    }

    private void repaint(long delay) {
        if (container != null && fBounds != null) {
            container.repaint(delay, fBounds.x, fBounds.y, fBounds.width, fBounds.height);
        }

    }

    private int getIntAttr(Attribute name, int deflt) {
        AttributeSet attr = getElement().getAttributes();
        if (attr.isDefined(name)) {
            String val = (String) attr.getAttribute(name);
            int i;
            if (val == null) {
                i = deflt;
            } else {
                try {
                    i = Math.max(0, Integer.parseInt(val));
                } catch (NumberFormatException var7) {
                    i = deflt;
                }
            }

            return i;
        } else {
            return deflt;
        }
    }

    private void sync() {
        int s = state;
        if ((s & 32) != 0) {
            refreshImage();
        }

        s = state;
        if ((s & 16) != 0) {
            synchronized (this) {
                state = (state | 16) ^ 16;
            }

            setPropertiesFromAttributes();
        }

    }

    private void refreshImage() {
        synchronized (this) {
            state = (state | 1 | 32 | 4 | 8) ^ 44;
            image = null;
            width = height = 0;
        }

        try {
            loadImage();
            updateImageSize();
        } finally {
            synchronized (this) {
                state = (state | 1) ^ 1;
            }
        }

    }

    private void loadImage() {
        try {
            image = loadNewImage();
        } catch (Exception var2) {
            image = null;
            var2.printStackTrace();
        }

    }

    private Image loadNewImage() throws Exception {
        String source = getImageSource();
        return source == null ? null : SwingUtil.loadImage(source);
    }


    private void updateImageSize() {
        boolean newWidth = false;
        boolean newHeight = false;
        int newState = 0;
        Image newImage = getImage();
        if (newImage != null) {
            int newWidth1 = getIntAttr(Attribute.WIDTH, -1);
            if (newWidth1 > 0) {
                newState |= 4;
            }

            int newHeight1 = getIntAttr(Attribute.HEIGHT, -1);
            if (newHeight1 > 0) {
                newState |= 8;
            }

            if (newWidth1 <= 0) {
                newWidth1 = newImage.getWidth(imageObserver);
                if (newWidth1 <= 0) {
                    newWidth1 = 38;
                }
            }

            if (newHeight1 <= 0) {
                newHeight1 = newImage.getHeight(imageObserver);
                if (newHeight1 <= 0) {
                    newHeight1 = 38;
                }
            }

            if ((newState & 12) != 0) {
                Toolkit.getDefaultToolkit().prepareImage(newImage, newWidth1, newHeight1, imageObserver);
            } else {
                Toolkit.getDefaultToolkit().prepareImage(newImage, -1, -1, imageObserver);
            }

            boolean createText = false;
            synchronized (this) {
                if (image == null) {
                    createText = true;
                    if ((newState & 4) == 4) {
                        width = newWidth1;
                    }

                    if ((newState & 8) == 8) {
                        height = newHeight1;
                    }
                } else {
                    if ((newState & 4) == 4 || width == 0) {
                        width = newWidth1;
                    }

                    if ((newState & 8) == 8 || height == 0) {
                        height = newHeight1;
                    }
                }

                state |= newState;
                state = (state | 1) ^ 1;
            }

            if (createText) {
                updateAltTextView();
            }
        } else {
            width = height = 38;
            updateAltTextView();
        }

    }

    private void updateAltTextView() {
        String text = getAltText();
        if (text != null) {
            ExtendedImageView.ImageLabelView newView = new ExtendedImageView.ImageLabelView(getElement(), text);
            synchronized (this) {
                altView = newView;
            }
        }

    }

    private View getAltView() {
        View view;
        synchronized (this) {
            view = altView;
        }

        if (view != null && view.getParent() == null) {
            view.setParent(getParent());
        }

        return view;
    }

    private void safePreferenceChanged() {
        if (SwingUtilities.isEventDispatchThread()) {
            Document doc = getDocument();
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument) doc).readLock();
            }

            preferenceChanged(null, true, true);
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument) doc).readUnlock();
            }
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    safePreferenceChanged();
                }
            });
        }

    }

    private class ImageHandler implements ImageObserver {
        private ImageHandler() {
        }

        public boolean imageUpdate(Image img, int flags, int x, int y, int newWidth, int newHeight) {
            if ((img == image || img == disabledImage) && image != null && getParent() != null) {
                if ((flags & 192) != 0) {
                    repaint(0L);
                    ExtendedImageView changed1 = ExtendedImageView.this;
                    synchronized (ExtendedImageView.this) {
                        if (image == img) {
                            image = null;
                            if ((state & 4) != 4) {
                                width = 38;
                            }

                            if ((state & 8) != 8) {
                                height = 38;
                            }
                        } else {
                            disabledImage = null;
                        }

                        if ((state & 1) == 1) {
                            return false;
                        }
                    }

                    updateAltTextView();
                    safePreferenceChanged();
                    return false;
                } else {
                    if (image == img) {
                        short changed = 0;
                        if ((flags & 2) != 0 && !getElement().getAttributes().isDefined(Attribute.HEIGHT)) {
                            changed = (short) (changed | 1);
                        }

                        if ((flags & 1) != 0 && !getElement().getAttributes().isDefined(Attribute.WIDTH)) {
                            changed = (short) (changed | 2);
                        }

                        ExtendedImageView var8 = ExtendedImageView.this;
                        synchronized (ExtendedImageView.this) {
                            if ((changed & 1) == 1 && (state & 4) == 0) {
                                width = newWidth;
                            }

                            if ((changed & 2) == 2 && (state & 8) == 0) {
                                height = newHeight;
                            }

                            if ((state & 1) == 1) {
                                return true;
                            }
                        }

                        if (changed != 0) {
                            safePreferenceChanged();
                            return true;
                        }
                    }

                    if ((flags & 48) != 0) {
                        repaint(0L);
                    } else if ((flags & 8) != 0 && ExtendedImageView.sIsInc) {
                        repaint((long) ExtendedImageView.sIncRate);
                    }

                    return (flags & 32) == 0;
                }
            } else {
                return false;
            }
        }
    }

    private class ImageLabelView extends InlineView {
        private Segment segment;
        private Color fg;

        ImageLabelView(Element e, String text) {
            super(e);
            reset(text);
        }

        public void reset(String text) {
            segment = new Segment(text.toCharArray(), 0, text.length());
        }

        public void paint(Graphics g, Shape a) {
            GlyphPainter painter = getGlyphPainter();
            if (painter != null) {
                g.setColor(getForeground());
                painter.paint(this, g, a, getStartOffset(), getEndOffset());
            }

        }

        public Segment getText(int p0, int p1) {
            if (p0 >= 0 && p1 <= segment.array.length) {
                segment.offset = p0;
                segment.count = p1 - p0;
                return segment;
            } else {
                throw new RuntimeException("ImageLabelView: Stale view");
            }
        }

        public int getStartOffset() {
            return 0;
        }

        public int getEndOffset() {
            return segment.array.length;
        }

        public View breakView(int axis, int p0, float pos, float len) {
            return this;
        }

        public Color getForeground() {
            View parent;
            if (fg == null && (parent = getParent()) != null) {
                Document doc = getDocument();
                AttributeSet attr = parent.getAttributes();
                if (attr != null && doc instanceof StyledDocument) {
                    fg = ((StyledDocument) doc).getForeground(attr);
                }
            }

            return fg;
        }
    }
}
