package ru.turikhay.tlauncher.ui.center;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.util.U;

public class CenterPanelPipe_unused extends JComponent implements ResizeableComponent {
   private static final long serialVersionUID = -4783918557432088966L;
   public static final int PIPE_LONG = 30;
   public static final int PIPE_SHORT = 20;
   private final CenterPanel parent;
   private Component targetComponent;
   private CenterPanelPipe_unused.PipeOrientation pipeOrientation;
   private int pipeX;
   private int pipeY;

   protected CenterPanelPipe_unused(CenterPanel panel) {
      this.parent = panel;
      this.pipeOrientation = null;
      this.setOpaque(false);
   }

   public CenterPanel getParent() {
      return this.parent;
   }

   public CenterPanelPipe_unused.PipeOrientation getOrientation() {
      return this.pipeOrientation;
   }

   public boolean canDisplay() {
      return this.pipeOrientation != null && this.targetComponent != null;
   }

   public Component getTarget() {
      return this.targetComponent;
   }

   public void setTarget(Component comp) {
      this.targetComponent = comp;
      this.onResize();
   }

   public void onResize() {
      if (this.targetComponent != null) {
         MainPane pane = this.parent.tlauncher.getFrame().mp;
         Point parentPoint = pane.getLocationOf(this.parent);
         Point targetPoint = pane.getLocationOf(this.targetComponent);
         byte safeZone = 24;
         int targetX = targetPoint.x + this.targetComponent.getWidth() / 2;
         int targetY = targetPoint.y + this.targetComponent.getHeight() / 2;
         int parentMinX = parentPoint.x;
         int parentMaxX = parentMinX + this.parent.getWidth();
         int parentMinY = parentPoint.y;
         int parentMaxY = parentMinY + this.parent.getHeight();
         boolean fitsX = U.interval(parentMinX + safeZone, parentMaxX - safeZone, targetX);
         boolean fitsY = U.interval(parentMinY + safeZone, parentMaxY - safeZone, targetY);
         this.pipeOrientation = null;
         byte pipeWidth;
         byte pipeHeight;
         if (fitsX) {
            pipeWidth = 30;
            pipeHeight = 20;
            this.pipeX = targetX - pipeWidth / 2;
            if (targetY <= parentMinY) {
               this.pipeY = parentMinY - pipeWidth;
               this.pipeOrientation = CenterPanelPipe_unused.PipeOrientation.UP;
            } else if (targetY > parentMaxY) {
               this.pipeY = parentMaxY + pipeHeight;
               this.pipeOrientation = CenterPanelPipe_unused.PipeOrientation.DOWN;
            }
         } else {
            if (!fitsY) {
               return;
            }

            pipeWidth = 20;
            pipeHeight = 30;
            this.pipeY = targetY - pipeHeight / 2;
            if (targetX <= parentMinX) {
               this.pipeX = parentMinX - pipeWidth;
               this.pipeOrientation = CenterPanelPipe_unused.PipeOrientation.LEFT;
            } else if (targetX > parentMaxY) {
               this.pipeX = parentMaxX + pipeHeight;
               this.pipeOrientation = CenterPanelPipe_unused.PipeOrientation.RIGHT;
            }
         }

         if (this.pipeOrientation != null) {
            this.setLocation(this.pipeX, this.pipeY);
            this.setSize(pipeWidth, pipeHeight);
         }
      }

   }

   public void paintComponent(Graphics g0) {
      if (this.targetComponent != null && this.pipeOrientation != null) {
         int maxX = this.getWidth();
         int maxY = this.getHeight();
         int midX = maxX / 2;
         int midY = maxY / 2;
         int[] triangleX = new int[3];
         int[] triangleY = new int[3];
         switch(this.pipeOrientation) {
         case LEFT:
            triangleX[0] = maxX;
            triangleY[0] = 0;
            triangleX[1] = 0;
            triangleY[1] = midY;
            triangleX[2] = maxX;
            triangleY[2] = maxY;
            break;
         case UP:
            triangleX[0] = 0;
            triangleY[0] = maxY;
            triangleX[1] = midX;
            triangleY[1] = 0;
            triangleX[2] = maxX;
            triangleY[2] = maxY;
            break;
         case RIGHT:
            triangleX[0] = 0;
            triangleY[0] = 0;
            triangleX[1] = maxX;
            triangleY[1] = midY;
            triangleX[2] = 0;
            triangleY[2] = maxY;
            break;
         case DOWN:
            triangleX[0] = 0;
            triangleY[0] = 0;
            triangleX[1] = midX;
            triangleY[1] = maxY;
            triangleX[2] = maxX;
            triangleY[2] = 0;
            break;
         default:
            throw new IllegalArgumentException("Unknown orientation: " + this.pipeOrientation);
         }

         CenterPanelTheme theme = this.parent.getTheme();
         Graphics2D g = (Graphics2D)g0;
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g.setColor(theme.getPanelBackground());
         g.fillPolygon(triangleX, triangleY, 3);
         g.setColor(theme.getBorder());
         g.drawPolygon(triangleX, triangleY, 3);
      }

   }

   protected void log(Object... o) {
      U.log("[CPipe]", o);
   }

   public static enum PipeOrientation {
      LEFT,
      UP,
      RIGHT,
      DOWN;
   }
}
