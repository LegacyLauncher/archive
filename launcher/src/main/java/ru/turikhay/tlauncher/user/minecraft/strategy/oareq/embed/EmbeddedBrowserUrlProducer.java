package ru.turikhay.tlauncher.user.minecraft.strategy.oareq.embed;

import ru.turikhay.tlauncher.user.minecraft.oauth.OAuthApplication;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.AbstractOAuthUrlProducer;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.RedirectUrl;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public class EmbeddedBrowserUrlProducer extends AbstractOAuthUrlProducer {
    private static final RedirectUrl REDIRECT_URL =
            RedirectUrl.of("https://login.live.com/oauth20_desktop.srf");

    private final RedirectUrl redirectUrl;

    public EmbeddedBrowserUrlProducer(RedirectUrl redirectUrl) {
        super(OAuthApplication.OFFICIAL_LAUNCHER);
        this.redirectUrl = Objects.requireNonNull(redirectUrl, "redirectUrl");
    }

    public EmbeddedBrowserUrlProducer() {
        this(REDIRECT_URL);
    }

    public RedirectUrl getRedirectUrl() {
        return redirectUrl;
    }

    public URL buildLoginUrl(String state) throws URISyntaxException, MalformedURLException {
        return buildLoginUrl(redirectUrl.getUrl().toString(), state).build().toURL();
    }

    public URL buildLoginUrl() throws URISyntaxException, MalformedURLException {
        return buildLoginUrl(null);
    }
}
