package net.legacylauncher.user.minecraft;

import net.legacylauncher.user.minecraft.oauth.OAuthApplication;
import net.legacylauncher.user.minecraft.strategy.MinecraftAuthenticationException;
import net.legacylauncher.user.minecraft.strategy.gos.GameOwnershipValidator;
import net.legacylauncher.user.minecraft.strategy.mcsauth.MinecraftServicesAuthenticator;
import net.legacylauncher.user.minecraft.strategy.oatoken.MicrosoftOAuthToken;
import net.legacylauncher.user.minecraft.strategy.oatoken.refresh.MicrosoftOAuthTokenRefresher;
import net.legacylauncher.user.minecraft.strategy.pconv.MinecraftProfileConverter;
import net.legacylauncher.user.minecraft.strategy.preq.MinecraftProfileRequester;
import net.legacylauncher.user.minecraft.strategy.xb.auth.XboxLiveAuthenticator;
import net.legacylauncher.user.minecraft.strategy.xb.xsts.XSTSAuthenticator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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