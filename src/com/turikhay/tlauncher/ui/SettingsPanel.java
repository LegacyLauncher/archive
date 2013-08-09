package com.turikhay.tlauncher.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import javax.swing.BoxLayout;

public class SettingsPanel extends CenterPanel {
   private static final long serialVersionUID = -7723742073256801896L;
   final SettingsPanel instance = this;
   Panel javas;
   Checkbox javas_memory_c;
   Panel javas_memory;
   TextField javas_memory_t;
   Button javas_memory_b;
   Checkbox javas_gamedir_c;
   TextField javas_gamedir_t;
   Checkbox javas_sizes_c;
   Panel javas_sizes;
   TextField javas_sizes_t_w;
   TextField javas_sizes_t_h;
   Label javas_sizes_l_x;
   Panel versions;
   Panel versions_r;
   Checkbox[] versions_r_c = new Checkbox[3];
   Label versions_l;
   Panel jvm;
   Checkbox jvm_path_c;
   TextField jvm_path_t;
   Checkbox jvm_args_c;
   TextField jvm_args_t;
   Panel gui;
   Checkbox gui_sun;
   Panel select;
   Button select_save;
   Button select_cancel;

   public SettingsPanel(TLauncherFrame f) {
      super(f);
      this.insets = new Insets(5, 8, 18, 8);
      this.javas = new Panel(new GridLayout(0, 2));
      this.javas.add(new Label("JVM"));
      this.javas.add(this.del(0));
      this.javas_memory = new Panel(new BorderLayout());
      this.javas_memory_t = new TextField("1024");
      this.javas_memory_b = new Button("?");
      this.javas_memory_b.setPreferredSize(new Dimension(30, this.javas_memory_b.getHeight()));
      this.javas_memory.add("Center", this.javas_memory_t);
      this.javas_memory.add("East", this.javas_memory_b);
      this.javas_memory_c = new Checkbox("Memory:");
      this.javas.add(this.javas_memory_c);
      this.javas.add(this.javas_memory);
      this.javas_gamedir_c = new Checkbox("Gamedir:");
      this.javas_gamedir_t = new TextField();
      this.javas_gamedir_t.setPreferredSize(new Dimension(100, this.javas_gamedir_t.getHeight()));
      this.javas.add(this.javas_gamedir_c);
      this.javas.add(this.javas_gamedir_t);
      this.javas_sizes = new Panel();
      this.javas_sizes.setLayout(new BoxLayout(this.javas_sizes, 0));
      this.javas_sizes_t_w = new TextField();
      this.javas_sizes.add(this.javas_sizes_t_w);
      this.javas_sizes_l_x = new Label("x");
      this.javas_sizes_l_x.setAlignment(1);
      this.javas_sizes_l_x.setPreferredSize(new Dimension(15, this.javas_sizes_l_x.getHeight()));
      this.javas_sizes.add(this.javas_sizes_l_x);
      this.javas_sizes_t_h = new TextField();
      this.javas_sizes.add(this.javas_sizes_t_h);
      this.javas_sizes_c = new Checkbox("Sizes:");
      this.javas.add(this.javas_sizes_c);
      this.javas.add(this.javas_sizes);
      this.versions = new Panel(new GridLayout(0, 2));
      this.versions.add(new Label("Versions"));
      this.javas.add(this.del(0));
      this.add(this.javas);
   }

   protected void block() {
   }

   protected void unblock() {
   }

   public void setError(String message) {
   }
}
