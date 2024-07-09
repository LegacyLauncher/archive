package net.legacylauncher.user.minecraft.strategy.preq;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import net.legacylauncher.user.minecraft.strategy.rqnpr.*;
import org.apache.hc.client5.http.fluent.Request;

import java.io.IOException;

@Slf4j
public class MinecraftProfileRequester
        extends RequestAndParseStrategy<MinecraftServicesToken, MinecraftOAuthProfile> {
    public MinecraftProfileRequester() {
        this(new HttpClientRequester<>(token ->
                Request.get("https://api.minecraftservices.com/minecraft/profile")
                        .addHeader("Authorization", "Bearer " + token.getAccessToken()))
        );
    }

    MinecraftProfileRequester(Requester<MinecraftServicesToken> requester) {
        this(requester, GsonParser.withDashlessUUIDAdapter(MinecraftOAuthProfile.class));
    }

    MinecraftProfileRequester(Requester<MinecraftServicesToken> requester,
                              Parser<MinecraftOAuthProfile> parser) {
        super(log, requester, parser);
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
