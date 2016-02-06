package ru.turikhay.tlauncher.ui.background;

import java.awt.Color;
import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.background.slide.SlideBackground;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;

public class BackgroundHolder extends ExtendedLayeredPane {
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

   public void startBackground() {
      if (this.currentBackground != null && this.currentBackground instanceof AnimatedBackground) {
         ((AnimatedBackground)this.currentBackground).startBackground();
      }

   }

   public void suspendBackground() {
      if (this.currentBackground != null && this.currentBackground instanceof AnimatedBackground) {
         ((AnimatedBackground)this.currentBackground).suspendBackground();
      }

   }
}
