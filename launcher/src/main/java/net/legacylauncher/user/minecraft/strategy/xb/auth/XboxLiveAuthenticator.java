package net.legacylauncher.user.minecraft.strategy.xb.auth;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.user.minecraft.oauth.OAuthApplication;
import net.legacylauncher.user.minecraft.strategy.oatoken.MicrosoftOAuthToken;
import net.legacylauncher.user.minecraft.strategy.rqnpr.HttpClientRequester;
import net.legacylauncher.user.minecraft.strategy.rqnpr.InvalidResponseException;
import net.legacylauncher.user.minecraft.strategy.rqnpr.Requester;
import net.legacylauncher.user.minecraft.strategy.xb.XboxServiceAuthStrategy;
import net.legacylauncher.user.minecraft.strategy.xb.XboxServiceAuthenticationResponse;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;

import java.io.IOException;

@Slf4j
public class XboxLiveAuthenticator extends XboxServiceAuthStrategy {
    public XboxLiveAuthenticator(OAuthApplication application) {
        super(log, new HttpClientRequester<>(accessToken ->
                Request.post("https://user.auth.xboxlive.com/user/authenticate")
                        .bodyString(
                                String.format(java.util.Locale.ROOT, "{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"%s\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}",
                                        (application.usesWeirdXboxTokenPrefix() ? "d=" : "") + accessToken),
                                ContentType.APPLICATION_JSON
                        ))
        );
    }

    XboxLiveAuthenticator(Requester<String> requester) {
        super(log, requester);
    }

    public XboxServiceAuthenticationResponse xboxLiveAuthenticate(String accessToken)
            throws XboxLiveAuthenticationException, IOException {
        try {
            return requestAndParse(accessToken);
        } catch (InvalidResponseException e) {
            throw new XboxLiveAuthenticationException(e);
        }
    }

    public XboxServiceAuthenticationResponse xboxLiveAuthenticate(MicrosoftOAuthToken token)
            throws XboxLiveAuthenticationException, IOException {
        return xboxLiveAuthenticate(token.getAccessToken());
    }
}
