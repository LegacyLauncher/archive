package ru.turikhay.tlauncher.managers;

import ru.turikhay.tlauncher.minecraft.auth.AccountListener;

public interface ProfileManagerListener extends AccountListener {
    void onProfilesRefreshed(ProfileManager var1);

    void onProfileManagerChanged(ProfileManager var1);
}
