package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.exceptions.TLauncherException;
import java.awt.AlphaComposite;
import java.awt.Button;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.ImageObserver;
import javax.imageio.ImageIO;

public class ImageButton extends Button {
   private static final long serialVersionUID = 1L;
   protected Image image;
   protected ImageButton.ImageRotation rotation;
   protected int margin;
   private boolean pressed;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$turikhay$tlauncher$ui$ImageButton$ImageRotation;

   protected ImageButton() {
      this.rotation = ImageButton.ImageRotation.CENTER;
      this.margin = 4;
      this.initListeners();
   }

   public ImageButton(String label, Image image, ImageButton.ImageRotation rotation, int margin) {
      super(label);
      this.rotation = ImageButton.ImageRotation.CENTER;
      this.margin = 4;
      this.image = image;
      this.rotation = rotation;
      this.margin = margin;
      this.initListeners();
   }

   public ImageButton(String label, Image image, ImageButton.ImageRotation rotation) {
      this(label, (Image)image, rotation, 4);
   }

   public ImageButton(String label, Image image) {
      this(label, image, ImageButton.ImageRotation.CENTER);
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
      return this.image;
   }

   public ImageButton.ImageRotation getRotation() {
      return this.rotation;
   }

   public int getMargin() {
      return this.margin;
   }

   public void update(Graphics g) {
      super.update(g);
      this.paint(g);
   }

   public void paint(Graphics g0) {
      if (this.image != null) {
         Graphics2D g = (Graphics2D)g0;
         String text = this.getLabel();
         boolean drawtext = text != null && text.length() > 0;
         FontMetrics fm = g.getFontMetrics();
         float opacity = this.isEnabled() ? 1.0F : 0.5F;
         int width = this.getWidth();
         int height = this.getHeight();
         int rmargin = this.margin;
         int offset = this.pressed ? 1 : 0;
         int iwidth = this.image.getWidth((ImageObserver)null);
         int iheight = this.image.getHeight((ImageObserver)null);
         int ix = false;
         int iy = height / 2 - iheight / 2;
         int twidth;
         if (drawtext) {
            twidth = fm.stringWidth(text);
         } else {
            rmargin = 0;
            twidth = 0;
         }

         int ix;
         switch($SWITCH_TABLE$com$turikhay$tlauncher$ui$ImageButton$ImageRotation()[this.rotation.ordinal()]) {
         case 1:
            ix = width / 2 - twidth / 2 - iwidth - rmargin;
            break;
         case 2:
            ix = width / 2 - iwidth / 2;
            break;
         case 3:
            ix = width / 2 + twidth / 2 + rmargin;
            break;
         default:
            throw new IllegalStateException("Unknown rotation!");
         }

         Composite c = g.getComposite();
         g.setComposite(AlphaComposite.getInstance(3, opacity));
         g.drawImage(this.image, ix + offset, iy + offset, (ImageObserver)null);
         g.setComposite(c);
         this.pressed = false;
      }
   }

   protected static Image loadImage(String path) {
      try {
         return ImageIO.read(SupportButton.class.getResource(path));
      } catch (Exception var2) {
         throw new TLauncherException("Cannot load required image (" + path + ")!", var2);
      }
   }

   private void initListeners() {
      this.addMouseListener(new MouseListener() {
         public void mouseClicked(MouseEvent e) {
         }

         public void mouseEntered(MouseEvent e) {
         }

         public void mouseExited(MouseEvent e) {
         }

         public void mousePressed(MouseEvent e) {
            ImageButton.this.pressed = true;
         }

         public void mouseReleased(MouseEvent e) {
         }
      });
      this.addKeyListener(new KeyListener() {
         public void keyPressed(KeyEvent e) {
            if (e.getExtendedKeyCode() == 32) {
               ImageButton.this.pressed = true;
            }
         }

         public void keyReleased(KeyEvent e) {
            ImageButton.this.pressed = false;
         }

         public void keyTyped(KeyEvent e) {
         }
      });
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$turikhay$tlauncher$ui$ImageButton$ImageRotation() {
      int[] var10000 = $SWITCH_TABLE$com$turikhay$tlauncher$ui$ImageButton$ImageRotation;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[ImageButton.ImageRotation.values().length];

         try {
            var0[ImageButton.ImageRotation.CENTER.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[ImageButton.ImageRotation.LEFT.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[ImageButton.ImageRotation.RIGHT.ordinal()] = 3;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$turikhay$tlauncher$ui$ImageButton$ImageRotation = var0;
         return var0;
      }
   }

   public static enum ImageRotation {
      LEFT,
      CENTER,
      RIGHT;
   }
}
