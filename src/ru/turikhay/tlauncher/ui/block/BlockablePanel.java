package ru.turikhay.tlauncher.ui.block;

import java.awt.Container;
import java.awt.LayoutManager;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;

public class BlockablePanel extends ExtendedPanel implements Blockable {
   public BlockablePanel(LayoutManager layout) {
      super(layout);
   }

   public BlockablePanel() {
   }

   public void block(Object reason) {
      this.setEnabled(false);
      Blocker.blockComponents((Container)this, (Object)reason);
   }

   public void unblock(Object reason) {
      this.setEnabled(true);
      Blocker.unblockComponents((Container)this, (Object)reason);
   }
}
