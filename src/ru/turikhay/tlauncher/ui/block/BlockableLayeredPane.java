package ru.turikhay.tlauncher.ui.block;

import java.awt.Container;
import javax.swing.JLayeredPane;

public class BlockableLayeredPane extends JLayeredPane implements Blockable {
   public void block(Object reason) {
      Blocker.blockComponents((Container)this, (Object)reason);
   }

   public void unblock(Object reason) {
      Blocker.unblockComponents((Container)this, (Object)reason);
   }
}
