package net.legacylauncher.user.minecraft;

import net.legacylauncher.user.MinecraftUser;
import net.legacylauncher.user.minecraft.oauth.OAuthApplication;
import net.legacylauncher.user.minecraft.strategy.MinecraftAuthenticationException;
import net.legacylauncher.user.minecraft.strategy.gos.GameOwnershipValidator;
import net.legacylauncher.user.minecraft.strategy.mcsauth.MinecraftServicesAuthenticator;
import net.legacylauncher.user.minecraft.strategy.oareq.OAuthUrlParser;
import net.legacylauncher.user.minecraft.strategy.oareq.embed.BrowserConfiguration;
import net.legacylauncher.user.minecraft.strategy.oareq.embed.EmbeddedBrowserStrategy;
import net.legacylauncher.user.minecraft.strategy.oareq.embed.EmbeddedBrowserUrlProducer;
import net.legacylauncher.user.minecraft.strategy.oareq.embed.JavaFXBrowser;
import net.legacylauncher.user.minecraft.strategy.oareq.lcserv.LocalServerConfiguration;
import net.legacylauncher.user.minecraft.strategy.oareq.lcserv.LocalServerStrategy;
import net.legacylauncher.user.minecraft.strategy.oareq.lcserv.LocalServerUrlProducer;
import net.legacylauncher.user.minecraft.strategy.oareq.lcserv.nanohttpd.NanoHttpdLocalServer;
import net.legacylauncher.user.minecraft.strategy.oatoken.exchange.MicrosoftOAuthCodeExchanger;
import net.legacylauncher.user.minecraft.strategy.pconv.MinecraftProfileConverter;
import net.legacylauncher.user.minecraft.strategy.preq.MinecraftProfileRequester;
import net.legacylauncher.user.minecraft.strategy.preq.create.MinecraftProfileCreator;
import net.legacylauncher.user.minecraft.strategy.preq.create.ProfileCreatorUserInterface;
import net.legacylauncher.user.minecraft.strategy.xb.auth.XboxLiveAuthenticator;
import net.legacylauncher.user.minecraft.strategy.xb.xsts.XSTSAuthenticator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

public class MinecraftOAuthAuthenticateTest {
    private static final Logger LOGGER = LogManager.getLogger(MinecraftOAuthAuthenticateTest.class);

    @Test
    @Disabled
    void usingLocalServer() throws InterruptedException, MinecraftAuthenticationException, IOException, TimeoutException {
        OAuthApplication app = OAuthApplication.TL;
        MinecraftOAuthAuthenticate flow = new MinecraftOAuthAuthenticate(
                new LocalServerStrategy(
                        url -> System.out.println("Please open this URL in the browser: " + url),
                        new LocalServerUrlProducer(),
                        new NanoHttpdLocalServer(
                                new OAuthUrlParser(),
                                new LocalServerUrlProducer()
                        ),
                        new LocalServerConfiguration(
                                "localhost",
                                Arrays.asList(49521, 49522, 49523, 49524),
                                "",
                                null
                        )
                ),
                new MicrosoftOAuthCodeExchanger(app),
                new XboxLiveAuthenticator(app),
                new XSTSAuthenticator(),
                new MinecraftServicesAuthenticator(),
                new GameOwnershipValidator(),
                new MinecraftProfileRequester(),
                new MinecraftProfileCreator(profileCreatorUserInterface),
                new MinecraftProfileConverter()
        );
        MinecraftUser authenticate = flow.authenticate();
        LOGGER.info(authenticate);
    }

    @Test
    @Disabled
    void usingEmbeddedBrowser() throws InterruptedException, MinecraftAuthenticationException, IOException, TimeoutException {
        OAuthApplication app = OAuthApplication.OFFICIAL_LAUNCHER;
        MinecraftOAuthAuthenticate flow = new MinecraftOAuthAuthenticate(
                new EmbeddedBrowserStrategy(
                        new JavaFXBrowser(true),
                        new EmbeddedBrowserUrlProducer(),
                        new BrowserConfiguration(
                                "Microsoft Authentication",
                                Collections.emptyList()
                        ),
                        new OAuthUrlParser()
                ),
                new MicrosoftOAuthCodeExchanger(app),
                new XboxLiveAuthenticator(app),
                new XSTSAuthenticator(),
                new MinecraftServicesAuthenticator(),
                new GameOwnershipValidator(),
                new MinecraftProfileRequester(),
                new MinecraftProfileCreator(profileCreatorUserInterface),
                new MinecraftProfileConverter()
        );
        MinecraftUser authenticate = flow.authenticate();
        LOGGER.info(authenticate);
    }

    private final ProfileCreatorUserInterface profileCreatorUserInterface = new ProfileCreatorUserInterface() {
        @Override
        public String requestProfileName() {
            return JOptionPane.showInputDialog("Please select Minecraft profile name:");
        }

        @Override
        public void showProfileUnavailableMessage(String profileName) {
            JOptionPane.showMessageDialog(null, "This profile name (" + profileName + ") is not available");
        }
    };
}