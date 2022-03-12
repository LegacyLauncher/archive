package ru.turikhay.tlauncher.user.minecraft.strategy.oareq.lcserv;

import org.apache.http.client.utils.URIBuilder;
import ru.turikhay.tlauncher.user.minecraft.oauth.OAuthApplication;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.AbstractOAuthUrlProducer;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class LocalServerUrlProducer extends AbstractOAuthUrlProducer {

    public LocalServerUrlProducer() {
        super(OAuthApplication.TL);
    }

    public String buildLoginUrl(LocalServerSelectedConfiguration selectedConfiguration)
            throws MalformedURLException, URISyntaxException {
        return buildLoginUrl(
                buildRedirectUrl(selectedConfiguration),
                selectedConfiguration.getState()
        ).build().toURL().toString();
    }

    public String buildRedirectUrl(LocalServerSelectedConfiguration selectedConfiguration)
            throws URISyntaxException {
        URIBuilder url = new URIBuilder(
                String.format(java.util.Locale.ROOT,
                        "http://%s:%d",
                        selectedConfiguration.getHost(),
                        selectedConfiguration.getPort()
                )
        );
        url.setPath(selectedConfiguration.getPath());
        return url.build().toASCIIString();
    }

}
