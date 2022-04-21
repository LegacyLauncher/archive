package ru.turikhay.tlauncher.user.minecraft.strategy.preq;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.client.fluent.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.*;

import java.io.IOException;

public class MinecraftProfileRequester
        extends RequestAndParseStrategy<MinecraftServicesToken, MinecraftOAuthProfile> {
    private static final Logger LOGGER = LogManager.getLogger(MinecraftProfileRequester.class);

    public MinecraftProfileRequester() {
        this(new HttpClientRequester<>(token ->
                Request.Get("https://api.minecraftservices.com/minecraft/profile")
                        .addHeader("Authorization", "Bearer " + token.getAccessToken()))
        );
    }

    MinecraftProfileRequester(Requester<MinecraftServicesToken> requester) {
        this(requester, GsonParser.withDashlessUUIDAdapter(MinecraftOAuthProfile.class));
    }

    MinecraftProfileRequester(Requester<MinecraftServicesToken> requester,
                              Parser<MinecraftOAuthProfile> parser) {
        super(LOGGER, requester, parser);
    }

    public MinecraftOAuthProfile requestProfile(MinecraftServicesToken token)
            throws MinecraftProfileRequestException, IOException {
        try {
            return requestAndParse(token);
        } catch (InvalidResponseException e) {
            JsonObject response = e.getResponseAsJson();
            JsonElement errorElement = response.get("error");
            if (errorElement != null && errorElement.isJsonPrimitive() && "NOT_FOUND".equals(errorElement.getAsString())) {
                throw new ProfileNotCreatedException(e);
            }
            throw new MinecraftProfileRequestException(e);
        }
    }
}
