package com.turikhay.tlauncher.managers;

import com.turikhay.tlauncher.minecraft.auth.AccountListener;

public interface ProfileManagerListener extends AccountListener {
	public void onProfilesRefreshed(ProfileManager pm);

	public void onProfileManagerChanged(ProfileManager pm);
}
