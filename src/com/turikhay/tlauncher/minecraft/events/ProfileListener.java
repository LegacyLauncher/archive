package com.turikhay.tlauncher.minecraft.events;

import com.turikhay.tlauncher.minecraft.profiles.ProfileManager;

public interface ProfileListener {
   void onProfilesRefreshed(ProfileManager var1);

   void onProfileManagerChanged(ProfileManager var1);
}
