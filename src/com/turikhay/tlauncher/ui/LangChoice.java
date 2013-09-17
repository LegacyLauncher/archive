package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.TLauncher;
import java.awt.Choice;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class LangChoice extends Choice {
   private static final long serialVersionUID = 570709181645184106L;
   private Map replacer = new LinkedHashMap();

   LangChoice(SettingsForm sf) {
      String[] available = TLauncher.SUPPORTED_LOCALE;
      Locale[] var6;
      int var5 = (var6 = Locale.getAvailableLocales()).length;

      for(int var4 = 0; var4 < var5; ++var4) {
         Locale loc = var6[var4];
         String[] var10 = available;
         int var9 = available.length;

         for(int var8 = 0; var8 < var9; ++var8) {
            String id = var10[var8];
            if (loc.toString().equals(id)) {
               String curdisp = loc.getDisplayCountry(Locale.ENGLISH) + " (" + id + ")";
               this.replacer.put(curdisp, id);
               this.add(curdisp);
               if (TLauncher.getInstance().locale == loc) {
                  this.select(curdisp);
               }
               break;
            }
         }
      }

   }

   public String getValue() {
      return (String)this.replacer.get(this.getSelectedItem());
   }

   public void selectValue(String id) {
      Iterator var3 = this.replacer.entrySet().iterator();

      while(var3.hasNext()) {
         Entry curen = (Entry)var3.next();
         if (((String)curen.getKey()).equals(id)) {
            this.select((String)curen.getValue());
         }
      }

   }
}
