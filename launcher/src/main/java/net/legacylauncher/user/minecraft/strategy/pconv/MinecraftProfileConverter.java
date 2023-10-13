package net.legacylauncher.user.minecraft.strategy.pconv;

import net.legacylauncher.user.MinecraftUser;
import net.legacylauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import net.legacylauncher.user.minecraft.strategy.oatoken.MicrosoftOAuthToken;
import net.legacylauncher.user.minecraft.strategy.preq.MinecraftOAuthProfile;

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
