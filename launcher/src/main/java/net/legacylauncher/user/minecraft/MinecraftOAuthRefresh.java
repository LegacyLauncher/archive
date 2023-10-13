package net.legacylauncher.user.minecraft;

import net.legacylauncher.user.MinecraftUser;
import net.legacylauncher.user.minecraft.strategy.MinecraftAuthenticationException;
import net.legacylauncher.user.minecraft.strategy.gos.GameOwnershipValidator;
import net.legacylauncher.user.minecraft.strategy.mcsauth.MinecraftServicesAuthenticator;
import net.legacylauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import net.legacylauncher.user.minecraft.strategy.oatoken.MicrosoftOAuthToken;
import net.legacylauncher.user.minecraft.strategy.oatoken.refresh.MicrosoftOAuthTokenRefresher;
import net.legacylauncher.user.minecraft.strategy.pconv.MinecraftProfileConverter;
import net.legacylauncher.user.minecraft.strategy.preq.MinecraftOAuthProfile;
import net.legacylauncher.user.minecraft.strategy.preq.MinecraftProfileRequestException;
import net.legacylauncher.user.minecraft.strategy.preq.MinecraftProfileRequester;
import net.legacylauncher.user.minecraft.strategy.xb.XboxServiceAuthenticationResponse;
import net.legacylauncher.user.minecraft.strategy.xb.auth.XboxLiveAuthenticator;
import net.legacylauncher.user.minecraft.strategy.xb.xsts.XSTSAuthenticator;

import java.io.IOException;

public class MinecraftOAuthRefresh {

    private final MicrosoftOAuthTokenRefresher tokenRefresher;
    private final XboxLiveAuthenticator xboxLiveAuthenticator;
    private final XSTSAuthenticator xstsAuthenticator;
    private final MinecraftServicesAuthenticator minecraftServicesAuthenticator;
    private final GameOwnershipValidator gameOwnershipValidator;
    private final MinecraftProfileRequester minecraftProfileRequester;
    private final MinecraftProfileConverter minecraftProfileConverter;

    public MinecraftOAuthRefresh(MicrosoftOAuthTokenRefresher tokenRefresher,
                                 XboxLiveAuthenticator xboxLiveAuthenticator,
                                 XSTSAuthenticator xstsAuthenticator,
                                 MinecraftServicesAuthenticator minecraftServicesAuthenticator,
                                 GameOwnershipValidator gameOwnershipValidator,
                                 MinecraftProfileRequester minecraftProfileRequester,
                                 MinecraftProfileConverter minecraftProfileConverter) {
        this.tokenRefresher = tokenRefresher;
        this.xboxLiveAuthenticator = xboxLiveAuthenticator;
        this.xstsAuthenticator = xstsAuthenticator;
        this.minecraftServicesAuthenticator = minecraftServicesAuthenticator;
        this.gameOwnershipValidator = gameOwnershipValidator;
        this.minecraftProfileRequester = minecraftProfileRequester;
        this.minecraftProfileConverter = minecraftProfileConverter;
    }

    public MinecraftUser refreshIfNeeded(MicrosoftOAuthToken microsoftToken, MinecraftServicesToken minecraftToken)
            throws MinecraftAuthenticationException, IOException {
        // You also can check if these tokens are actually expired by:
        // if(!microsoftToken.isExpired() && !minecraftToken.isExpired()) return null;
        MinecraftOAuthProfile profile;
        try {
            profile = minecraftProfileRequester.requestProfile(minecraftToken);
        } catch (MinecraftProfileRequestException e) {
            return refreshExplicitly(microsoftToken);
        }
        return minecraftProfileConverter.convertToMinecraftUser(microsoftToken, minecraftToken, profile);
    }

    public MinecraftUser refreshExplicitly(MicrosoftOAuthToken expiredToken)
            throws MinecraftAuthenticationException, IOException {
        MicrosoftOAuthToken token = tokenRefresher.refreshToken(expiredToken);
        XboxServiceAuthenticationResponse xlToken = xboxLiveAuthenticator.xboxLiveAuthenticate(token);
        XboxServiceAuthenticationResponse xstsToken = xstsAuthenticator.xstsAuthenticate(xlToken.getToken());
        MinecraftServicesToken mcsToken = minecraftServicesAuthenticator.minecraftServicesAuthenticate(xstsToken);
        gameOwnershipValidator.checkGameOwnership(mcsToken);
        MinecraftOAuthProfile profile = minecraftProfileRequester.requestProfile(mcsToken);
        return minecraftProfileConverter.convertToMinecraftUser(token, mcsToken, profile);
    }
}
