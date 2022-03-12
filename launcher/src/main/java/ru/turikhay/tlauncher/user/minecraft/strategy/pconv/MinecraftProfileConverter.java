package ru.turikhay.tlauncher.user.minecraft.strategy.pconv;

import ru.turikhay.tlauncher.user.MinecraftUser;
import ru.turikhay.tlauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import ru.turikhay.tlauncher.user.minecraft.strategy.oatoken.MicrosoftOAuthToken;
import ru.turikhay.tlauncher.user.minecraft.strategy.preq.MinecraftOAuthProfile;

public class MinecraftProfileConverter {
    public MinecraftUser convertToMinecraftUser(MicrosoftOAuthToken microsoftToken,
                                                MinecraftServicesToken minecraftToken,
                                                MinecraftOAuthProfile profile)
            throws MinecraftProfileConversionException {
        try {
            return new MinecraftUser(
                    profile,
                    microsoftToken,
                    minecraftToken
            );
        } catch (RuntimeException rE) {
            throw new MinecraftProfileConversionException(rE);
        }
    }
}
