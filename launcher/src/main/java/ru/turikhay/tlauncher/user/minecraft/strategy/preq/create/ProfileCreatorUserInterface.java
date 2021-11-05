package ru.turikhay.tlauncher.user.minecraft.strategy.preq.create;

public interface ProfileCreatorUserInterface {
    String requestProfileName() throws InterruptedException;
    void showProfileUnavailableMessage(String profileName);
}
