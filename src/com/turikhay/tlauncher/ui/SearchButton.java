package com.turikhay.tlauncher.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SearchButton extends ImageButton implements Blockable {
   private static final long serialVersionUID = 682875580116075167L;

   SearchButton(final SearchPanel sp) {
      this.image = loadImage("search.png");
      this.setForeground(Color.white);
      this.setBackground(Color.black);
      this.setPreferredSize(new Dimension(30, this.getHeight()));
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            sp.search();
         }
      });
   }

   public void block(Object reason) {
      this.setEnabled(false);
   }

   public void unblock(Object reason) {
      this.setEnabled(true);
   }
}
