package net.legacylauncher.user.minecraft.strategy.mcsauth;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.user.minecraft.strategy.rqnpr.*;
import net.legacylauncher.user.minecraft.strategy.xb.XboxServiceAuthenticationResponse;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;

import java.io.IOException;

@Slf4j
public class MinecraftServicesAuthenticator
        extends RequestAndParseStrategy<XboxServiceAuthenticationResponse, MinecraftServicesToken> {
    public MinecraftServicesAuthenticator() {
        this(new HttpClientRequester<>(r ->
                Request.post("https://api.minecraftservices.com/authentication/login_with_xbox")
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
        super(log, requester, parser);
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
