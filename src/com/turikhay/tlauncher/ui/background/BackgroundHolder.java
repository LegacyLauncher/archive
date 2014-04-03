package com.turikhay.tlauncher.ui.background;

import com.turikhay.tlauncher.ui.MainPane;
import com.turikhay.tlauncher.ui.background.slide.SlideBackground;
import com.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;
import java.awt.Color;

public class BackgroundHolder extends ExtendedLayeredPane {
   private static final long serialVersionUID = 8722087129402330131L;
   public final MainPane pane;
   private Background currentBackground;
   public final BackgroundCover cover;
   public final SlideBackground SLIDE_BACKGROUND;

   public BackgroundHolder(MainPane parent) {
      super(parent);
      this.pane = parent;
      this.cover = new BackgroundCover(this);
      this.SLIDE_BACKGROUND = new SlideBackground(this);
      this.add(this.cover, Integer.MAX_VALUE);
   }

   public Background getBackgroundPane() {
      return this.currentBackground;
   }

   public void setBackground(Background background, boolean animate) {
      if (background == null) {
         throw new NullPointerException();
      } else {
         Color coverColor = background.getCoverColor();
         if (coverColor == null) {
            coverColor = Color.black;
         }

         this.cover.setColor(coverColor, animate);
         this.cover.makeCover(animate);
         if (this.currentBackground != null) {
            this.remove(this.currentBackground);
         }

         this.currentBackground = background;
         this.add(this.currentBackground);
         this.cover.removeCover(animate);
      }
   }

   public void showBackground() {
      this.cover.removeCover();
   }

   public void hideBackground() {
      this.cover.makeCover();
   }

   public void startBackground() {
      if (this.currentBackground != null) {
         if (this.currentBackground instanceof AnimatedBackground) {
            ((AnimatedBackground)this.currentBackground).startBackground();
         }

      }
   }

   public void suspendBackground() {
      if (this.currentBackground != null) {
         if (this.currentBackground instanceof AnimatedBackground) {
            ((AnimatedBackground)this.currentBackground).suspendBackground();
         }

      }
   }

   public void stopBackground() {
      if (this.currentBackground != null) {
         if (this.currentBackground instanceof AnimatedBackground) {
            ((AnimatedBackground)this.currentBackground).stopBackground();
         }

      }
   }
}
