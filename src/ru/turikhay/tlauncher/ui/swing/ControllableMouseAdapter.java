package ru.turikhay.tlauncher.ui.swing;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ControllableMouseAdapter implements MouseListener {
   private ControllableMouseAdapter.MouseEventHandler click;
   private ControllableMouseAdapter.MouseEventHandler press;
   private ControllableMouseAdapter.MouseEventHandler release;
   private ControllableMouseAdapter.MouseEventHandler enter;
   private ControllableMouseAdapter.MouseEventHandler exit;

   public ControllableMouseAdapter.MouseEventHandler getOnClick() {
      return this.click;
   }

   public ControllableMouseAdapter setOnClick(ControllableMouseAdapter.MouseEventHandler handler) {
      this.click = handler;
      return this;
   }

   public ControllableMouseAdapter.MouseEventHandler getOnPress() {
      return this.press;
   }

   public ControllableMouseAdapter setOnPress(ControllableMouseAdapter.MouseEventHandler handler) {
      this.press = handler;
      return this;
   }

   public ControllableMouseAdapter.MouseEventHandler getOnRelease() {
      return this.release;
   }

   public ControllableMouseAdapter setOnRelease(ControllableMouseAdapter.MouseEventHandler handler) {
      this.release = handler;
      return this;
   }

   public ControllableMouseAdapter.MouseEventHandler getOnEnter() {
      return this.enter;
   }

   public ControllableMouseAdapter setOnEnter(ControllableMouseAdapter.MouseEventHandler handler) {
      this.enter = handler;
      return this;
   }

   public ControllableMouseAdapter.MouseEventHandler getOnExit() {
      return this.exit;
   }

   public ControllableMouseAdapter setOnExit(ControllableMouseAdapter.MouseEventHandler handler) {
      this.exit = handler;
      return this;
   }

   public final void mouseClicked(MouseEvent e) {
      if (this.click != null) {
         this.click.handleEvent(e);
      }

   }

   public final void mousePressed(MouseEvent e) {
      if (this.press != null) {
         this.press.handleEvent(e);
      }

   }

   public final void mouseReleased(MouseEvent e) {
      if (this.release != null) {
         this.release.handleEvent(e);
      }

   }

   public final void mouseEntered(MouseEvent e) {
      if (this.enter != null) {
         this.enter.handleEvent(e);
      }

   }

   public final void mouseExited(MouseEvent e) {
      if (this.exit != null) {
         this.exit.handleEvent(e);
      }

   }

   public interface MouseEventHandler {
      void handleEvent(MouseEvent var1);
   }
}
