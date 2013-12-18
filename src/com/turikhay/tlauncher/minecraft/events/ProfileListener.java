package com.turikhay.tlauncher.minecraft.events;

import com.turikhay.tlauncher.minecraft.profiles.ProfileManager;

public interface ProfileListener {
	void onProfilesRefreshed(ProfileManager pm);
	void onProfileManagerChanged(ProfileManager pm);
}
