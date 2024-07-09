package net.legacylauncher.user.minecraft.strategy.oatoken.exchange;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.user.minecraft.oauth.OAuthApplication;
import net.legacylauncher.user.minecraft.strategy.oareq.MicrosoftOAuthExchangeCode;
import net.legacylauncher.user.minecraft.strategy.oatoken.MicrosoftOAuthToken;
import net.legacylauncher.user.minecraft.strategy.rqnpr.*;
import org.apache.hc.client5.http.fluent.Form;
import org.apache.hc.client5.http.fluent.Request;

import java.io.IOException;

@Slf4j
public class MicrosoftOAuthCodeExchanger
        extends RequestAndParseStrategy<MicrosoftOAuthExchangeCode, MicrosoftOAuthToken> {
    public MicrosoftOAuthCodeExchanger(String clientId) {
        this(new HttpClientRequester<>(code ->
                Request.post("https://login.live.com/oauth20_token.srf").bodyForm(
                        Form.form()
                                .add("client_id", clientId)
                                .add("code", code.getCode())
                                .add("grant_type", "authorization_code")
                                .add("redirect_uri", code.getRedirectUrl().toString())
                                .build()
                )
        ), GsonParser.lowerCaseWithUnderscores(MicrosoftOAuthToken.class));
    }

    public MicrosoftOAuthCodeExchanger(OAuthApplication application) {
        this(application.getClientId());
    }

    MicrosoftOAuthCodeExchanger(Requester<MicrosoftOAuthExchangeCode> requester,
                                Parser<MicrosoftOAuthToken> parser) {
        super(log, requester, parser);
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
