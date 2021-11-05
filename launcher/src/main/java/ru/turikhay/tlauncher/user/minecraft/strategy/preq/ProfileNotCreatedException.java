package ru.turikhay.tlauncher.user.minecraft.strategy.preq;

public class ProfileNotCreatedException extends MinecraftProfileRequestException {
    public ProfileNotCreatedException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getShortKey() {
        return "minecraft_profile_not_created";
    }
}
