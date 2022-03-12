package ru.turikhay.tlauncher.user.minecraft;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.turikhay.tlauncher.user.minecraft.oauth.OAuthApplication;
import ru.turikhay.tlauncher.user.minecraft.strategy.MinecraftAuthenticationException;
import ru.turikhay.tlauncher.user.minecraft.strategy.gos.GameOwnershipValidator;
import ru.turikhay.tlauncher.user.minecraft.strategy.mcsauth.MinecraftServicesAuthenticator;
import ru.turikhay.tlauncher.user.minecraft.strategy.oatoken.MicrosoftOAuthToken;
import ru.turikhay.tlauncher.user.minecraft.strategy.oatoken.refresh.MicrosoftOAuthTokenRefresher;
import ru.turikhay.tlauncher.user.minecraft.strategy.pconv.MinecraftProfileConverter;
import ru.turikhay.tlauncher.user.minecraft.strategy.preq.MinecraftProfileRequester;
import ru.turikhay.tlauncher.user.minecraft.strategy.xb.auth.XboxLiveAuthenticator;
import ru.turikhay.tlauncher.user.minecraft.strategy.xb.xsts.XSTSAuthenticator;

import java.io.IOException;
import java.time.Instant;

class MinecraftOAuthRefreshTest {

    @Test
    @Disabled
    void testRefreshIfNeeded() throws MinecraftAuthenticationException, IOException {
        OAuthApplication app = OAuthApplication.TL;
        MinecraftOAuthRefresh refresh = new MinecraftOAuthRefresh(
                new MicrosoftOAuthTokenRefresher(app),
                new XboxLiveAuthenticator(app),
                new XSTSAuthenticator(),
                new MinecraftServicesAuthenticator(),
                new GameOwnershipValidator(),
                new MinecraftProfileRequester(),
                new MinecraftProfileConverter()
        );
        refresh.refreshExplicitly(new MicrosoftOAuthToken(
                "yourAccessToken",
                "yourRefreshToken",
                Instant.now()
        ));
    }

}