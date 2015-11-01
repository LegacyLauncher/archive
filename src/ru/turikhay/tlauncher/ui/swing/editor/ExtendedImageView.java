package ru.turikhay.tlauncher.ui.swing.editor;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Segment;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.GlyphView.GlyphPainter;
import javax.swing.text.Position.Bias;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.InlineView;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;
import javax.xml.bind.DatatypeConverter;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

public class ExtendedImageView extends View {
   private static String base64s = "data:image/";
   private static String base64e = ";base64,";
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
      return (String)this.getElement().getAttributes().getAttribute(Attribute.ALT);
   }

   public String getImageSource() {
      return (String)this.getElement().getAttributes().getAttribute(Attribute.SRC);
   }

   public Icon getNoImageIcon() {
      return (Icon)UIManager.getLookAndFeelDefaults().get("html.missingImage");
   }

   public Icon getLoadingImageIcon() {
      return (Icon)UIManager.getLookAndFeelDefaults().get("html.pendingImage");
   }

   public Image getImage() {
      this.sync();
      return this.image;
   }

   private Image getImage(boolean enabled) {
      Image img = this.getImage();
      if (!enabled) {
         if (this.disabledImage == null) {
            this.disabledImage = GrayFilter.createDisabledImage(img);
         }

         img = this.disabledImage;
      }

      return img;
   }

   public void setLoadsSynchronously(boolean newValue) {
      synchronized(this) {
         if (newValue) {
            this.state |= 64;
         } else {
            this.state = (this.state | 64) ^ 64;
         }

      }
   }

   public boolean getLoadsSynchronously() {
      return (this.state & 64) != 0;
   }

   protected StyleSheet getStyleSheet() {
      HTMLDocument doc = (HTMLDocument)this.getDocument();
      return doc.getStyleSheet();
   }

   public AttributeSet getAttributes() {
      this.sync();
      return this.attr;
   }

   public String getToolTipText(float x, float y, Shape allocation) {
      return this.getAltText();
   }

   protected void setPropertiesFromAttributes() {
      StyleSheet sheet = this.getStyleSheet();
      this.attr = sheet.getViewAttributes(this);
      this.borderSize = (short)this.getIntAttr(Attribute.BORDER, 0);
      this.leftInset = this.rightInset = (short)(this.getIntAttr(Attribute.HSPACE, 0) + this.borderSize);
      this.topInset = this.bottomInset = (short)(this.getIntAttr(Attribute.VSPACE, 0) + this.borderSize);
      this.borderColor = ((StyledDocument)this.getDocument()).getForeground(this.getAttributes());
      AttributeSet attr = this.getElement().getAttributes();
      Object alignment = attr.getAttribute(Attribute.ALIGN);
      this.vAlign = 1.0F;
      if (alignment != null) {
         String alignment1 = alignment.toString();
         if ("top".equals(alignment1)) {
            this.vAlign = 0.0F;
         } else if ("middle".equals(alignment1)) {
            this.vAlign = 0.5F;
         }
      }

      AttributeSet anchorAttr = (AttributeSet)attr.getAttribute(Tag.A);
      if (anchorAttr != null && anchorAttr.isDefined(Attribute.HREF)) {
         synchronized(this) {
            this.state |= 2;
         }
      } else {
         synchronized(this) {
            this.state = (this.state | 2) ^ 2;
         }
      }

   }

   public void setParent(View parent) {
      View oldParent = this.getParent();
      super.setParent(parent);
      this.container = parent != null ? this.getContainer() : null;
      if (oldParent != parent) {
         synchronized(this) {
            this.state |= 16;
         }
      }

   }

   public void changedUpdate(DocumentEvent e, Shape a, ViewFactory f) {
      super.changedUpdate(e, a, f);
      synchronized(this) {
         this.state |= 48;
      }

      this.preferenceChanged((View)null, true, true);
   }

   public void paint(Graphics g, Shape a) {
      this.sync();
      Rectangle rect = a instanceof Rectangle ? (Rectangle)a : a.getBounds();
      Rectangle clip = g.getClipBounds();
      this.fBounds.setBounds(rect);
      this.paintHighlights(g, a);
      this.paintBorder(g, rect);
      if (clip != null) {
         g.clipRect(rect.x + this.leftInset, rect.y + this.topInset, rect.width - this.leftInset - this.rightInset, rect.height - this.topInset - this.bottomInset);
      }

      Container host = this.getContainer();
      Image img = this.getImage(host == null || host.isEnabled());
      Icon icon;
      if (img != null) {
         if (!this.hasPixels(img)) {
            icon = this.getLoadingImageIcon();
            if (icon != null) {
               icon.paintIcon(host, g, rect.x + this.leftInset, rect.y + this.topInset);
            }
         } else {
            g.drawImage(img, rect.x + this.leftInset, rect.y + this.topInset, this.width, this.height, this.imageObserver);
         }
      } else {
         icon = this.getNoImageIcon();
         if (icon != null) {
            icon.paintIcon(host, g, rect.x + this.leftInset, rect.y + this.topInset);
         }

         View view = this.getAltView();
         if (view != null && ((this.state & 4) == 0 || this.width > 38)) {
            Rectangle altRect = new Rectangle(rect.x + this.leftInset + 38, rect.y + this.topInset, rect.width - this.leftInset - this.rightInset - 38, rect.height - this.topInset - this.bottomInset);
            view.paint(g, altRect);
         }
      }

      if (clip != null) {
         g.setClip(clip.x, clip.y, clip.width, clip.height);
      }

   }

   private void paintHighlights(Graphics g, Shape shape) {
      if (this.container instanceof JTextComponent) {
         JTextComponent tc = (JTextComponent)this.container;
         Highlighter h = tc.getHighlighter();
         if (h instanceof LayeredHighlighter) {
            ((LayeredHighlighter)h).paintLayeredHighlights(g, this.getStartOffset(), this.getEndOffset(), shape, tc, this);
         }
      }

   }

   private void paintBorder(Graphics g, Rectangle rect) {
      Color color = this.borderColor;
      if ((this.borderSize > 0 || this.image == null) && color != null) {
         int xOffset = this.leftInset - this.borderSize;
         int yOffset = this.topInset - this.borderSize;
         g.setColor(color);
         short n = this.image == null ? 1 : this.borderSize;

         for(int counter = 0; counter < n; ++counter) {
            g.drawRect(rect.x + xOffset + counter, rect.y + yOffset + counter, rect.width - counter - counter - xOffset - xOffset - 1, rect.height - counter - counter - yOffset - yOffset - 1);
         }
      }

   }

   public float getPreferredSpan(int axis) {
      this.sync();
      if (axis == 0 && (this.state & 4) == 4) {
         this.getPreferredSpanFromAltView(axis);
         return (float)(this.width + this.leftInset + this.rightInset);
      } else if (axis == 1 && (this.state & 8) == 8) {
         this.getPreferredSpanFromAltView(axis);
         return (float)(this.height + this.topInset + this.bottomInset);
      } else {
         Image image = this.getImage();
         if (image != null) {
            switch(axis) {
            case 0:
               return (float)(this.width + this.leftInset + this.rightInset);
            case 1:
               return (float)(this.height + this.topInset + this.bottomInset);
            default:
               throw new IllegalArgumentException("Invalid axis: " + axis);
            }
         } else {
            View view = this.getAltView();
            float retValue = 0.0F;
            if (view != null) {
               retValue = view.getPreferredSpan(axis);
            }

            switch(axis) {
            case 0:
               return retValue + (float)(this.width + this.leftInset + this.rightInset);
            case 1:
               return retValue + (float)(this.height + this.topInset + this.bottomInset);
            default:
               throw new IllegalArgumentException("Invalid axis: " + axis);
            }
         }
      }
   }

   public float getAlignment(int axis) {
      switch(axis) {
      case 1:
         return this.vAlign;
      default:
         return super.getAlignment(axis);
      }
   }

   public Shape modelToView(int pos, Shape a, Bias b) throws BadLocationException {
      int p0 = this.getStartOffset();
      int p1 = this.getEndOffset();
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
      Rectangle alloc = (Rectangle)a;
      if (x < (float)(alloc.x + alloc.width)) {
         bias[0] = Bias.Forward;
         return this.getStartOffset();
      } else {
         bias[0] = Bias.Backward;
         return this.getEndOffset();
      }
   }

   public void setSize(float width, float height) {
      this.sync();
      if (this.getImage() == null) {
         View view = this.getAltView();
         if (view != null) {
            view.setSize(Math.max(0.0F, width - (float)(38 + this.leftInset + this.rightInset)), Math.max(0.0F, height - (float)(this.topInset + this.bottomInset)));
         }
      }

   }

   private boolean hasPixels(Image image) {
      return image != null && image.getHeight(this.imageObserver) > 0 && image.getWidth(this.imageObserver) > 0;
   }

   private float getPreferredSpanFromAltView(int axis) {
      if (this.getImage() == null) {
         View view = this.getAltView();
         if (view != null) {
            return view.getPreferredSpan(axis);
         }
      }

      return 0.0F;
   }

   private void repaint(long delay) {
      if (this.container != null && this.fBounds != null) {
         this.container.repaint(delay, this.fBounds.x, this.fBounds.y, this.fBounds.width, this.fBounds.height);
      }

   }

   private int getIntAttr(Attribute name, int deflt) {
      AttributeSet attr = this.getElement().getAttributes();
      if (attr.isDefined(name)) {
         String val = (String)attr.getAttribute(name);
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
      int s = this.state;
      if ((s & 32) != 0) {
         this.refreshImage();
      }

      s = this.state;
      if ((s & 16) != 0) {
         synchronized(this) {
            this.state = (this.state | 16) ^ 16;
         }

         this.setPropertiesFromAttributes();
      }

   }

   private void refreshImage() {
      synchronized(this) {
         this.state = (this.state | 1 | 32 | 4 | 8) ^ 44;
         this.image = null;
         this.width = this.height = 0;
      }

      boolean var11 = false;

      try {
         var11 = true;
         this.loadImage();
         this.updateImageSize();
         var11 = false;
      } finally {
         if (var11) {
            synchronized(this) {
               this.state = (this.state | 1) ^ 1;
            }
         }
      }

      synchronized(this) {
         this.state = (this.state | 1) ^ 1;
      }
   }

   private void loadImage() {
      try {
         this.image = this.loadNewImage();
      } catch (Exception var2) {
         this.image = null;
         var2.printStackTrace();
      }

   }

   private Image loadNewImage() throws Exception {
      String source = this.getImageSource();
      if (source == null) {
         return null;
      } else if (!source.startsWith(base64s)) {
         URL src1 = U.makeURL(source);
         return src1 == null ? null : Images.loadMagnifiedImage(src1);
      } else {
         int src = base64s.length();
         String newImage = source.substring(src, src + 4);
         if (!newImage.startsWith("png") && !newImage.startsWith("jpg")) {
            if (!newImage.equals("jpeg")) {
               return null;
            }

            src += 4;
         } else {
            src += 3;
         }

         if (!source.substring(src, src + base64e.length()).equals(base64e)) {
            return null;
         } else {
            src += base64e.length();
            byte[] cache = DatatypeConverter.parseBase64Binary(source.substring(src));
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(cache));
            return image.getScaledInstance(SwingUtil.magnify(image.getWidth()), SwingUtil.magnify(image.getHeight()), 4);
         }
      }
   }

   private void updateImageSize() {
      boolean newWidth = false;
      boolean newHeight = false;
      int newState = 0;
      Image newImage = this.getImage();
      if (newImage != null) {
         int newWidth1 = this.getIntAttr(Attribute.WIDTH, -1);
         if (newWidth1 > 0) {
            newState |= 4;
         }

         int newHeight1 = this.getIntAttr(Attribute.HEIGHT, -1);
         if (newHeight1 > 0) {
            newState |= 8;
         }

         if (newWidth1 <= 0) {
            newWidth1 = newImage.getWidth(this.imageObserver);
            if (newWidth1 <= 0) {
               newWidth1 = 38;
            }
         }

         if (newHeight1 <= 0) {
            newHeight1 = newImage.getHeight(this.imageObserver);
            if (newHeight1 <= 0) {
               newHeight1 = 38;
            }
         }

         if ((newState & 12) != 0) {
            Toolkit.getDefaultToolkit().prepareImage(newImage, newWidth1, newHeight1, this.imageObserver);
         } else {
            Toolkit.getDefaultToolkit().prepareImage(newImage, -1, -1, this.imageObserver);
         }

         boolean createText = false;
         synchronized(this) {
            if (this.image == null) {
               createText = true;
               if ((newState & 4) == 4) {
                  this.width = newWidth1;
               }

               if ((newState & 8) == 8) {
                  this.height = newHeight1;
               }
            } else {
               if ((newState & 4) == 4 || this.width == 0) {
                  this.width = newWidth1;
               }

               if ((newState & 8) == 8 || this.height == 0) {
                  this.height = newHeight1;
               }
            }

            this.state |= newState;
            this.state = (this.state | 1) ^ 1;
         }

         if (createText) {
            this.updateAltTextView();
         }
      } else {
         this.width = this.height = 38;
         this.updateAltTextView();
      }

   }

   private void updateAltTextView() {
      String text = this.getAltText();
      if (text != null) {
         ExtendedImageView.ImageLabelView newView = new ExtendedImageView.ImageLabelView(this.getElement(), text);
         synchronized(this) {
            this.altView = newView;
         }
      }

   }

   private View getAltView() {
      View view;
      synchronized(this) {
         view = this.altView;
      }

      if (view != null && view.getParent() == null) {
         view.setParent(this.getParent());
      }

      return view;
   }

   private void safePreferenceChanged() {
      if (SwingUtilities.isEventDispatchThread()) {
         Document doc = this.getDocument();
         if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readLock();
         }

         this.preferenceChanged((View)null, true, true);
         if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readUnlock();
         }
      } else {
         SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               ExtendedImageView.this.safePreferenceChanged();
            }
         });
      }

   }

   private class ImageLabelView extends InlineView {
      private Segment segment;
      private Color fg;

      ImageLabelView(Element e, String text) {
         super(e);
         this.reset(text);
      }

      public void reset(String text) {
         this.segment = new Segment(text.toCharArray(), 0, text.length());
      }

      public void paint(Graphics g, Shape a) {
         GlyphPainter painter = this.getGlyphPainter();
         if (painter != null) {
            g.setColor(this.getForeground());
            painter.paint(this, g, a, this.getStartOffset(), this.getEndOffset());
         }

      }

      public Segment getText(int p0, int p1) {
         if (p0 >= 0 && p1 <= this.segment.array.length) {
            this.segment.offset = p0;
            this.segment.count = p1 - p0;
            return this.segment;
         } else {
            throw new RuntimeException("ImageLabelView: Stale view");
         }
      }

      public int getStartOffset() {
         return 0;
      }

      public int getEndOffset() {
         return this.segment.array.length;
      }

      public View breakView(int axis, int p0, float pos, float len) {
         return this;
      }

      public Color getForeground() {
         View parent;
         if (this.fg == null && (parent = this.getParent()) != null) {
            Document doc = this.getDocument();
            AttributeSet attr = parent.getAttributes();
            if (attr != null && doc instanceof StyledDocument) {
               this.fg = ((StyledDocument)doc).getForeground(attr);
            }
         }

         return this.fg;
      }
   }

   private class ImageHandler implements ImageObserver {
      private ImageHandler() {
      }

      public boolean imageUpdate(Image img, int flags, int x, int y, int newWidth, int newHeight) {
         if ((img == ExtendedImageView.this.image || img == ExtendedImageView.this.disabledImage) && ExtendedImageView.this.image != null && ExtendedImageView.this.getParent() != null) {
            if ((flags & 192) != 0) {
               ExtendedImageView.this.repaint(0L);
               ExtendedImageView changed1 = ExtendedImageView.this;
               synchronized(ExtendedImageView.this) {
                  if (ExtendedImageView.this.image == img) {
                     ExtendedImageView.this.image = null;
                     if ((ExtendedImageView.this.state & 4) != 4) {
                        ExtendedImageView.this.width = 38;
                     }

                     if ((ExtendedImageView.this.state & 8) != 8) {
                        ExtendedImageView.this.height = 38;
                     }
                  } else {
                     ExtendedImageView.this.disabledImage = null;
                  }

                  if ((ExtendedImageView.this.state & 1) == 1) {
                     return false;
                  }
               }

               ExtendedImageView.this.updateAltTextView();
               ExtendedImageView.this.safePreferenceChanged();
               return false;
            } else {
               if (ExtendedImageView.this.image == img) {
                  short changed = 0;
                  if ((flags & 2) != 0 && !ExtendedImageView.this.getElement().getAttributes().isDefined(Attribute.HEIGHT)) {
                     changed = (short)(changed | 1);
                  }

                  if ((flags & 1) != 0 && !ExtendedImageView.this.getElement().getAttributes().isDefined(Attribute.WIDTH)) {
                     changed = (short)(changed | 2);
                  }

                  ExtendedImageView var8 = ExtendedImageView.this;
                  synchronized(ExtendedImageView.this) {
                     if ((changed & 1) == 1 && (ExtendedImageView.this.state & 4) == 0) {
                        ExtendedImageView.this.width = newWidth;
                     }

                     if ((changed & 2) == 2 && (ExtendedImageView.this.state & 8) == 0) {
                        ExtendedImageView.this.height = newHeight;
                     }

                     if ((ExtendedImageView.this.state & 1) == 1) {
                        return true;
                     }
                  }

                  if (changed != 0) {
                     ExtendedImageView.this.safePreferenceChanged();
                     return true;
                  }
               }

               if ((flags & 48) != 0) {
                  ExtendedImageView.this.repaint(0L);
               } else if ((flags & 8) != 0 && ExtendedImageView.sIsInc) {
                  ExtendedImageView.this.repaint((long)ExtendedImageView.sIncRate);
               }

               return (flags & 32) == 0;
            }
         } else {
            return false;
         }
      }

      // $FF: synthetic method
      ImageHandler(Object x1) {
         this();
      }
   }
}
