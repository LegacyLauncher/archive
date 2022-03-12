package ru.turikhay.tlauncher.user.minecraft.strategy.xb.xsts;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.HttpClientRequester;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.InvalidResponseException;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.InvalidStatusCodeException;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.Requester;
import ru.turikhay.tlauncher.user.minecraft.strategy.xb.XboxServiceAuthStrategy;
import ru.turikhay.tlauncher.user.minecraft.strategy.xb.XboxServiceAuthenticationResponse;

import java.io.IOException;

public class XSTSAuthenticator extends XboxServiceAuthStrategy {
    private static final Logger LOGGER = LogManager.getLogger(XSTSAuthenticator.class);

    public XSTSAuthenticator() {
        super(LOGGER, new HttpClientRequester<>(xboxLiveToken ->
                Request.Post("https://xsts.auth.xboxlive.com/xsts/authorize")
                        .bodyString(
                                String.format(java.util.Locale.ROOT, "{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"%s\"]},\"RelyingParty\": \"rp://api.minecraftservices.com/\",\"TokenType\": \"JWT\"}", xboxLiveToken),
                                ContentType.APPLICATION_JSON
                        ))
        );
    }

    XSTSAuthenticator(Requester<String> requester) {
        super(LOGGER, requester);
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
}
