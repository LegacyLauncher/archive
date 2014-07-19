package ru.turikhay.tlauncher.managers;

import ru.turikhay.tlauncher.minecraft.auth.AccountListener;

public interface ProfileManagerListener extends AccountListener {
	public void onProfilesRefreshed(ProfileManager pm);

	public void onProfileManagerChanged(ProfileManager pm);
}
