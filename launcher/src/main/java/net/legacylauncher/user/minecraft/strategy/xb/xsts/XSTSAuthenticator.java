package net.legacylauncher.user.minecraft.strategy.xb.xsts;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.user.minecraft.strategy.rqnpr.HttpClientRequester;
import net.legacylauncher.user.minecraft.strategy.rqnpr.InvalidResponseException;
import net.legacylauncher.user.minecraft.strategy.rqnpr.InvalidStatusCodeException;
import net.legacylauncher.user.minecraft.strategy.rqnpr.Requester;
import net.legacylauncher.user.minecraft.strategy.xb.XboxServiceAuthStrategy;
import net.legacylauncher.user.minecraft.strategy.xb.XboxServiceAuthenticationResponse;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;

import java.io.IOException;

@Slf4j
public class XSTSAuthenticator extends XboxServiceAuthStrategy {
    public XSTSAuthenticator() {
        super(log, new HttpClientRequester<>(xboxLiveToken ->
                Request.post("https://xsts.auth.xboxlive.com/xsts/authorize")
                        .bodyString(
                                String.format(java.util.Locale.ROOT, "{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"%s\"]},\"RelyingParty\": \"rp://api.minecraftservices.com/\",\"TokenType\": \"JWT\"}", xboxLiveToken),
                                ContentType.APPLICATION_JSON
                        ))
        );
    }

    XSTSAuthenticator(Requester<String> requester) {
        super(log, requester);
    }

    private static XSTSAuthenticationException parseXErr(JsonObject response) {
        if (response.has("XErr")) {
            JsonElement xErr = response.get("XErr");
            if (xErr instanceof JsonPrimitive) {
                String code = xErr.getAsString();
                switch (code) {
                    case "2148916233":
                        return new NoXboxAccountException();
                    case "2148916235":
                        return new CountryNotAuthorizedException();
                    case "2148916238":
                        return new ChildAccountException();
                }
            }
        }
        return null;
    }

    public XboxServiceAuthenticationResponse xstsAuthenticate(String xboxLiveToken)
            throws XSTSAuthenticationException, IOException {
        try {
            return requestAndParse(xboxLiveToken);
        } catch (InvalidResponseException e) {
            if (e instanceof InvalidStatusCodeException) {
                if (((InvalidStatusCodeException) e).getStatusCode() == 401) {
                    JsonObject response = e.getResponseAsJson();
                    if (response != null) {
                        XSTSAuthenticationException e1 = parseXErr(response);
                        if (e1 != null) {
                            throw e1;
                        }
                    }
                }
            }
            throw new XSTSAuthenticationException(e);
        }
    }
}
