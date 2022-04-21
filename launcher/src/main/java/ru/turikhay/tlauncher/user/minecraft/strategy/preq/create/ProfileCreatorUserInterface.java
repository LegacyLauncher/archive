package ru.turikhay.tlauncher.user.minecraft.strategy.preq.create;

public interface ProfileCreatorUserInterface {
    String requestProfileName();

    void showProfileUnavailableMessage(String profileName);
}
