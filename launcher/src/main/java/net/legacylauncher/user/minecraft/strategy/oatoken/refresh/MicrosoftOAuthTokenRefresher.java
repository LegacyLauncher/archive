package net.legacylauncher.user.minecraft.strategy.oatoken.refresh;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.user.minecraft.oauth.OAuthApplication;
import net.legacylauncher.user.minecraft.strategy.oatoken.MicrosoftOAuthToken;
import net.legacylauncher.user.minecraft.strategy.rqnpr.*;
import org.apache.hc.client5.http.fluent.Form;
import org.apache.hc.client5.http.fluent.Request;

import java.io.IOException;

@Slf4j
public class MicrosoftOAuthTokenRefresher
        extends RequestAndParseStrategy<MicrosoftOAuthToken, MicrosoftOAuthToken> {
    public MicrosoftOAuthTokenRefresher(String clientId) {
        this(new HttpClientRequester<>(token ->
                Request.post("https://login.live.com/oauth20_token.srf").bodyForm(
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
        super(log, requester, parser);
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
