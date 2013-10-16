package com.turikhay.tlauncher.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SearchField extends LocalizableTextField {
   private static final long serialVersionUID = -6453744340240419870L;

   SearchField(final SearchPanel sp) {
      super("console.search.placeholder");
      this.ok_background = Color.black;
      this.ok_foreground = Color.white;
      this.setText((String)null);
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            sp.search();
         }
      });
   }

   protected boolean check(String text) {
      return true;
   }
}
