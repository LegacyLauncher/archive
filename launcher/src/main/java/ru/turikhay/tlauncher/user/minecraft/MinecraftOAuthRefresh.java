package ru.turikhay.tlauncher.user.minecraft;

import ru.turikhay.tlauncher.user.MinecraftUser;
import ru.turikhay.tlauncher.user.minecraft.strategy.MinecraftAuthenticationException;
import ru.turikhay.tlauncher.user.minecraft.strategy.gos.GameOwnershipValidator;
import ru.turikhay.tlauncher.user.minecraft.strategy.mcsauth.MinecraftServicesAuthenticator;
import ru.turikhay.tlauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import ru.turikhay.tlauncher.user.minecraft.strategy.oatoken.MicrosoftOAuthToken;
import ru.turikhay.tlauncher.user.minecraft.strategy.oatoken.refresh.MicrosoftOAuthTokenRefresher;
import ru.turikhay.tlauncher.user.minecraft.strategy.pconv.MinecraftProfileConverter;
import ru.turikhay.tlauncher.user.minecraft.strategy.preq.MinecraftOAuthProfile;
import ru.turikhay.tlauncher.user.minecraft.strategy.preq.MinecraftProfileRequestException;
import ru.turikhay.tlauncher.user.minecraft.strategy.preq.MinecraftProfileRequester;
import ru.turikhay.tlauncher.user.minecraft.strategy.xb.XboxServiceAuthenticationResponse;
import ru.turikhay.tlauncher.user.minecraft.strategy.xb.auth.XboxLiveAuthenticator;
import ru.turikhay.tlauncher.user.minecraft.strategy.xb.xsts.XSTSAuthenticator;

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
