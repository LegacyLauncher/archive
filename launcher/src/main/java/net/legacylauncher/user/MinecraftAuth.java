package net.legacylauncher.user;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.user.minecraft.oauth.OAuthApplication;
import net.legacylauncher.user.minecraft.strategy.MinecraftAuthenticationException;
import net.legacylauncher.user.minecraft.strategy.gos.GameOwnershipValidationException;
import net.legacylauncher.user.minecraft.strategy.gos.GameOwnershipValidator;
import net.legacylauncher.user.minecraft.strategy.mcsauth.MinecraftServicesAuthenticationException;
import net.legacylauncher.user.minecraft.strategy.mcsauth.MinecraftServicesAuthenticator;
import net.legacylauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import net.legacylauncher.user.minecraft.strategy.oatoken.MicrosoftOAuthToken;
import net.legacylauncher.user.minecraft.strategy.oatoken.refresh.MicrosoftOAuthTokenRefreshException;
import net.legacylauncher.user.minecraft.strategy.oatoken.refresh.MicrosoftOAuthTokenRefresher;
import net.legacylauncher.user.minecraft.strategy.preq.MinecraftOAuthProfile;
import net.legacylauncher.user.minecraft.strategy.preq.MinecraftProfileRequestException;
import net.legacylauncher.user.minecraft.strategy.preq.MinecraftProfileRequester;
import net.legacylauncher.user.minecraft.strategy.xb.XboxServiceAuthenticationResponse;
import net.legacylauncher.user.minecraft.strategy.xb.auth.XboxLiveAuthenticationException;
import net.legacylauncher.user.minecraft.strategy.xb.auth.XboxLiveAuthenticator;
import net.legacylauncher.user.minecraft.strategy.xb.xsts.XSTSAuthenticationException;
import net.legacylauncher.user.minecraft.strategy.xb.xsts.XSTSAuthenticator;

import java.io.IOException;

@Slf4j
public class MinecraftAuth implements Auth<MinecraftUser> {
    private final OAuthApplication APP = OAuthApplication.TL;

    @Override
    public void validate(MinecraftUser user) throws AuthException, IOException {
        if (user.getMinecraftToken().isExpired()) {
            refreshMinecraftAccessToken(user);
        }
        MinecraftProfileRequester requester = new MinecraftProfileRequester();
        MinecraftOAuthProfile profile;
        try {
            profile = requester.requestProfile(user.getMinecraftToken());
        } catch (MinecraftProfileRequestException e) {
            throw wrap(e);
        }
        log.info("Profile validated: {}", profile);
        user.setProfile(profile);
    }

    private void refreshMinecraftAccessToken(MinecraftUser user) throws AuthException, IOException {
        if (user.getMicrosoftToken().isExpired()) {
            refreshMicrosoftAccessToken(user);
        }
        XboxServiceAuthenticationResponse xstsToken = authenticateOnXboxServices(user);
        MinecraftServicesAuthenticator mcsAuthenticator = new MinecraftServicesAuthenticator();
        MinecraftServicesToken mcsToken;
        try {
            mcsToken = mcsAuthenticator.minecraftServicesAuthenticate(xstsToken);
        } catch (MinecraftServicesAuthenticationException e) {
            throw wrap(e);
        }
        GameOwnershipValidator ownershipValidator = new GameOwnershipValidator();
        try {
            ownershipValidator.checkGameOwnership(mcsToken);
        } catch (GameOwnershipValidationException e) {
            throw wrap(e);
        }
        user.setMinecraftToken(mcsToken);
    }

    private XboxServiceAuthenticationResponse authenticateOnXboxServices(MinecraftUser user)
            throws AuthException, IOException {
        XboxLiveAuthenticator xboxLiveAuthenticator = new XboxLiveAuthenticator(APP);
        XboxServiceAuthenticationResponse xboxLiveToken;
        try {
            xboxLiveToken = xboxLiveAuthenticator.xboxLiveAuthenticate(user.getMicrosoftToken());
        } catch (XboxLiveAuthenticationException e) {
            throw wrap(e);
        }
        XSTSAuthenticator xstsAuthenticator = new XSTSAuthenticator();
        XboxServiceAuthenticationResponse xstsToken;
        try {
            xstsToken = xstsAuthenticator.xstsAuthenticate(xboxLiveToken.getToken());
        } catch (XSTSAuthenticationException e) {
            throw wrap(e);
        }
        return xstsToken;
    }

    private void refreshMicrosoftAccessToken(MinecraftUser user) throws AuthException, IOException {
        MicrosoftOAuthTokenRefresher refresher = new MicrosoftOAuthTokenRefresher(APP);
        MicrosoftOAuthToken token;
        try {
            token = refresher.refreshToken(user.getMicrosoftToken());
        } catch (MicrosoftOAuthTokenRefreshException e) {
            throw wrap(e);
        }
        user.setMicrosoftToken(token);
    }

    private static AuthException wrap(MinecraftAuthenticationException e) {
        log.error("Couldn't validate the user", e);
        return new AuthException(e.toString(), e.getShortKey());
    }

}
