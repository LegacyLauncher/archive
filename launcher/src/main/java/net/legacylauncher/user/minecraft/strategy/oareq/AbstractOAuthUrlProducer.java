package net.legacylauncher.user.minecraft.strategy.oareq;

import net.legacylauncher.user.minecraft.oauth.OAuthApplication;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;
import java.util.Objects;

public abstract class AbstractOAuthUrlProducer {
    private static final String BASE_URL = "https://login.live.com/oauth20_authorize.srf";

    private final String baseUrl, clientId, scope;

    public AbstractOAuthUrlProducer(String baseUrl, String clientId, String scope) {
        this.baseUrl = baseUrl;
        this.clientId = clientId;
        this.scope = scope;
    }

    public AbstractOAuthUrlProducer(String clientId, String scope) {
        this(BASE_URL, clientId, scope);
    }

    public AbstractOAuthUrlProducer(OAuthApplication application) {
        this(application.getClientId(), application.getScope());
    }

    protected URIBuilder buildLoginUrl(String redirectUrl, String state) throws URISyntaxException {
        URIBuilder b = new URIBuilder(baseUrl);
        b.addParameter("client_id", clientId);
        b.addParameter("response_type", "code");
        b.addParameter("redirect_uri", Objects.requireNonNull(redirectUrl, "redirectUrl"));
        b.addParameter("scope", scope);
        if (state != null) {
            b.addParameter("state", state);
        }
        b.addParameter("prompt", "select_account");
        return b;
    }
}
