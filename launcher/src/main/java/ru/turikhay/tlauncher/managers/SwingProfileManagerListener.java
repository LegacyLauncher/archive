package ru.turikhay.tlauncher.managers;

import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorDatabase;
import ru.turikhay.util.SwingUtil;

public class SwingProfileManagerListener implements ProfileManagerListener {
    private final ProfileManagerListener listener;

    public SwingProfileManagerListener(ProfileManagerListener listener) {
        this.listener = listener;
    }

    @Override
    public void onProfilesRefreshed(ProfileManager var1) {
        SwingUtil.wait(() -> listener.onProfilesRefreshed(var1));
    }

    @Override
    public void onProfileManagerChanged(ProfileManager var1) {
        SwingUtil.wait(() -> listener.onProfileManagerChanged(var1));
    }

    @Override
    public void onAccountsRefreshed(AuthenticatorDatabase var1) {
        SwingUtil.wait(() -> listener.onAccountsRefreshed(var1));
    }
}
