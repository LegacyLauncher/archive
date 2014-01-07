package com.turikhay.tlauncher.ui;

import com.turikhay.util.StringUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SearchPanel extends BlockablePanel {
   private static final long serialVersionUID = -2659114952397165370L;
   protected Insets insets = new Insets(5, 10, 5, 10);
   ConsoleFrame cf;
   SearchField field;
   SearchPrefs prefs;
   SearchButton button;
   String regexp;
   Pattern pt;
   Matcher mt;
   int plastend;
   int lastend;

   SearchPanel(ConsoleFrame cf) {
      this.cf = cf;
      BorderLayout layout = new BorderLayout();
      layout.setVgap(2);
      layout.setHgap(5);
      this.setLayout(layout);
      this.setBackground(Color.black);
      this.setForeground(Color.white);
      this.add("Center", this.field = new SearchField(this));
      this.add("East", this.button = new SearchButton(this));
      this.add("South", this.prefs = new SearchPrefs(this));
   }

   public void search() {
      this.focus();
      String c_regexp = this.prefs.isRegExp() ? this.field.getValue() : StringUtil.addSlashes(this.field.getValue(), StringUtil.EscapeGroup.REGEXP);
      if (c_regexp != null && c_regexp.trim().length() != 0) {
         if (c_regexp.equalsIgnoreCase("fuck you")) {
            this.log("No, fuck you! :C");
            this.cf.scrollBottom();
         } else {
            int flags = 8;
            if (!this.prefs.isCaseSensetive()) {
               flags |= 2;
            }

            if (this.prefs.isWordSearch()) {
               c_regexp = "^[.]*(\\s){0,1}(" + c_regexp + ")(?:\\1|[\\s]|[\\s]{0,1})";
            }

            try {
               this.pt = Pattern.compile(c_regexp, flags);
            } catch (PatternSyntaxException var4) {
               this.log("Invalid pattern.\n", var4.toString());
               this.field.wrong((String)null);
               return;
            }

            if (!c_regexp.equals(this.regexp)) {
               this.regexp = c_regexp;
               this.lastend = 0;
            }

            this.find();
         }
      }
   }

   private void find() {
      this.field.ok();
      String text = this.cf.getOutput();
      this.mt = this.pt.matcher(text);
      if (!this.mt.find(this.lastend)) {
         if (this.prefs.isCycled() && this.plastend != this.lastend) {
            this.plastend = this.lastend = 0;
            this.search();
         } else {
            this.field.wrong((String)null);
         }
      } else {
         int group = this.prefs.isWordSearch() ? 2 : 0;
         int start = this.mt.start(group);
         this.lastend = this.mt.end(group);
         this.cf.update = false;
         this.cf.textArea.requestFocus();
         this.cf.textArea.select(start, this.lastend);
      }
   }

   protected void focus() {
      this.field.requestFocusInWindow();
   }

   public Insets getInsets() {
      return this.insets;
   }

   private void log(Object... o) {
      this.cf.c.log("[CONSOLE]", o);
      this.cf.scrollBottom();
   }

   protected void blockElement(Object reason) {
   }

   protected void unblockElement(Object reason) {
   }
}
