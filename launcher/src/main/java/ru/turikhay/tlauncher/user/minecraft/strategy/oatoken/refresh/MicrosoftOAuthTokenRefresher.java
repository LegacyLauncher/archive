package ru.turikhay.tlauncher.user.minecraft.strategy.oatoken.refresh;

import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.user.minecraft.oauth.OAuthApplication;
import ru.turikhay.tlauncher.user.minecraft.strategy.oatoken.MicrosoftOAuthToken;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.*;

import java.io.IOException;

public class MicrosoftOAuthTokenRefresher
        extends RequestAndParseStrategy<MicrosoftOAuthToken, MicrosoftOAuthToken> {

    private static final Logger LOGGER = LogManager.getLogger(MicrosoftOAuthTokenRefresher.class);

    public MicrosoftOAuthTokenRefresher(String clientId) {
        this(new HttpClientRequester<>(token ->
                Request.Post("https://login.live.com/oauth20_token.srf").bodyForm(
                        Form.form()
                                .add("client_id", clientId)
                                .add("refresh_token", token.getRefreshToken())
                                .add("grant_type", "refresh_token")
                                .build()
                )
        ), GsonParser.lowerCaseWithUnderscores(MicrosoftOAuthToken.class));
    }

    public MicrosoftOAuthTokenRefresher(OAuthApplication application) {
        this(application.getClientId());
    }

    protected MicrosoftOAuthTokenRefresher(Requester<MicrosoftOAuthToken> requester,
                                           Parser<MicrosoftOAuthToken> parser) {
        super(LOGGER, requester, parser);
    }

    public MicrosoftOAuthToken refreshToken(MicrosoftOAuthToken token)
            throws MicrosoftOAuthTokenRefreshException, IOException {
        try {
            return requestAndParse(token);
        } catch (InvalidResponseException e) {
            throw new MicrosoftOAuthTokenRefreshException(e);
        }
    }
}
