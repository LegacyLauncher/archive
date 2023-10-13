package net.legacylauncher.user.minecraft.strategy.oatoken.exchange;

import net.legacylauncher.user.minecraft.oauth.OAuthApplication;
import net.legacylauncher.user.minecraft.strategy.oareq.MicrosoftOAuthExchangeCode;
import net.legacylauncher.user.minecraft.strategy.oatoken.MicrosoftOAuthToken;
import net.legacylauncher.user.minecraft.strategy.rqnpr.*;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class MicrosoftOAuthCodeExchanger
        extends RequestAndParseStrategy<MicrosoftOAuthExchangeCode, MicrosoftOAuthToken> {

    private static final Logger LOGGER = LogManager.getLogger(MicrosoftOAuthCodeExchanger.class);

    public MicrosoftOAuthCodeExchanger(String clientId) {
        this(new HttpClientRequester<>(code ->
                Request.Post("https://login.live.com/oauth20_token.srf").bodyForm(
                        Form.form()
                                .add("client_id", clientId)
                                .add("code", code.getCode())
                                .add("grant_type", "authorization_code")
                                .add("redirect_uri", code.getRedirectUrl().getUrl().toString())
                                .build()
                )
        ), GsonParser.lowerCaseWithUnderscores(MicrosoftOAuthToken.class));
    }

    public MicrosoftOAuthCodeExchanger(OAuthApplication application) {
        this(application.getClientId());
    }

    MicrosoftOAuthCodeExchanger(Requester<MicrosoftOAuthExchangeCode> requester,
                                Parser<MicrosoftOAuthToken> parser) {
        super(LOGGER, requester, parser);
    }

    public MicrosoftOAuthToken exchangeMicrosoftOAuthCode(MicrosoftOAuthExchangeCode payload)
            throws MicrosoftOAuthCodeExchangeException, IOException {
        try {
            return requestAndParse(payload);
        } catch (InvalidResponseException e) {
            throw new MicrosoftOAuthCodeExchangeException(e);
        }
    }
}
