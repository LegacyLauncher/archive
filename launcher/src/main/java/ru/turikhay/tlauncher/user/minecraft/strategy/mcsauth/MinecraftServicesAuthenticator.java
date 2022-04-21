package ru.turikhay.tlauncher.user.minecraft.strategy.mcsauth;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.*;
import ru.turikhay.tlauncher.user.minecraft.strategy.xb.XboxServiceAuthenticationResponse;

import java.io.IOException;

public class MinecraftServicesAuthenticator
        extends RequestAndParseStrategy<XboxServiceAuthenticationResponse, MinecraftServicesToken> {
    private static final Logger LOGGER = LogManager.getLogger(MinecraftServicesAuthenticator.class);

    public MinecraftServicesAuthenticator() {
        this(new HttpClientRequester<>(r ->
                Request.Post("https://api.minecraftservices.com/authentication/login_with_xbox")
                        .bodyString(
                                String.format(java.util.Locale.ROOT, "{\"identityToken\":\"XBL3.0 x=%s;%s\"}", r.getUHS(), r.getToken()),
                                ContentType.APPLICATION_JSON
                        )));
    }

    MinecraftServicesAuthenticator(Requester<XboxServiceAuthenticationResponse> requester) {
        this(requester, GsonParser.lowerCaseWithUnderscores(MinecraftServicesToken.class));
    }

    MinecraftServicesAuthenticator(Requester<XboxServiceAuthenticationResponse> requester,
                                   Parser<MinecraftServicesToken> parser) {
        super(LOGGER, requester, parser);
    }

    public MinecraftServicesToken minecraftServicesAuthenticate(
            XboxServiceAuthenticationResponse xstsResponse)
            throws MinecraftServicesAuthenticationException, IOException {
        try {
            return requestAndParse(xstsResponse);
        } catch (InvalidResponseException e) {
            throw new MinecraftServicesAuthenticationException(e);
        }
    }
}
