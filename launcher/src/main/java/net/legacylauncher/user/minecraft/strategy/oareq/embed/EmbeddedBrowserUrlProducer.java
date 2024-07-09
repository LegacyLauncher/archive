package net.legacylauncher.user.minecraft.strategy.oareq.embed;

import net.legacylauncher.user.minecraft.oauth.OAuthApplication;
import net.legacylauncher.user.minecraft.strategy.oareq.AbstractOAuthUrlProducer;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public class EmbeddedBrowserUrlProducer extends AbstractOAuthUrlProducer {
    private static final URI REDIRECT_URL =
            URI.create("https://login.live.com/oauth20_desktop.srf");

    private final URI redirectUrl;

    public EmbeddedBrowserUrlProducer(URI redirectUrl) {
        super(OAuthApplication.OFFICIAL_LAUNCHER);
        this.redirectUrl = Objects.requireNonNull(redirectUrl, "redirectUrl");
    }

    public EmbeddedBrowserUrlProducer() {
        this(REDIRECT_URL);
    }

    public URI getRedirectUrl() {
        return redirectUrl;
    }

    public URL buildLoginUrl(String state) throws URISyntaxException, MalformedURLException {
        return buildLoginUrl(redirectUrl.toString(), state).build().toURL();
    }

    public URL buildLoginUrl() throws URISyntaxException, MalformedURLException {
        return buildLoginUrl(null);
    }
}
