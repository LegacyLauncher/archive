package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.minecraft.events.ProfileListener;
import com.turikhay.tlauncher.minecraft.profiles.ProfileLoader;
import com.turikhay.tlauncher.minecraft.profiles.ProfileManager;
import java.awt.Choice;
import java.util.Iterator;
import java.util.Map.Entry;

public class ProfileChoice extends Choice implements ProfileListener {
   private static final long serialVersionUID = 94188912646018620L;

   ProfileChoice(ProfileLoader pl) {
      pl.addListener(this);
      this.onProfilesRefreshed(pl.getSelected());
   }

   public void onProfilesRefreshed(ProfileManager pm) {
      this.removeAll();
      Iterator var3 = pm.getProfiles().entrySet().iterator();

      while(var3.hasNext()) {
         Entry en = (Entry)var3.next();
         this.add((String)en.getKey());
      }

   }

   public void onProfileManagerChanged(ProfileManager pm) {
   }
}
