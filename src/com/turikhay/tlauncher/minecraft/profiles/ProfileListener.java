package com.turikhay.tlauncher.minecraft.profiles;

import com.turikhay.tlauncher.minecraft.auth.AccountListener;

public interface ProfileListener extends AccountListener {
   void onProfilesRefreshed(ProfileManager var1);

   void onProfileManagerChanged(ProfileManager var1);
}
