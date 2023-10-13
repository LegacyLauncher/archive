package net.legacylauncher.managers;

import net.legacylauncher.minecraft.auth.AccountListener;

public interface ProfileManagerListener extends AccountListener {
    void onProfilesRefreshed(ProfileManager var1);

    void onProfileManagerChanged(ProfileManager var1);
}
