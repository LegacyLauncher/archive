package com.turikhay.tlauncher.ui.block;

import java.awt.LayoutManager;
import javax.swing.JPanel;

public class BlockablePanel extends JPanel implements Blockable {
   private static final long serialVersionUID = 1L;

   public BlockablePanel(LayoutManager layout, boolean isDoubleBuffered) {
      super(layout, isDoubleBuffered);
      this.init();
   }

   public BlockablePanel(LayoutManager layout) {
      super(layout);
      this.init();
   }

   public BlockablePanel() {
      this.init();
   }

   private void init() {
      Blocker.add(this);
   }

   public void block(Object reason) {
      this.setEnabled(false);
      Blocker.blockComponents(this, reason);
   }

   public void unblock(Object reason) {
      this.setEnabled(true);
      Blocker.unblockComponents(this, reason);
   }
}
