package net.legacylauncher.user.minecraft.strategy.preq.create;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import net.legacylauncher.user.minecraft.strategy.preq.MinecraftOAuthProfile;
import net.legacylauncher.user.minecraft.strategy.rqnpr.*;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;

import java.io.IOException;

@Slf4j
public class ProfileCreateRequest extends RequestAndParseStrategy<MinecraftServicesToken, MinecraftOAuthProfile> {
    public ProfileCreateRequest(String name) {
        this(new HttpClientRequester<>(token ->
                Request.post("https://api.minecraftservices.com/minecraft/profile")
                        .bodyString(profileName(name), ContentType.APPLICATION_JSON)
                        .addHeader("Authorization", "Bearer " + token.getAccessToken()))
        );
    }

    ProfileCreateRequest(Requester<MinecraftServicesToken> requester) {
        this(requester, GsonParser.withDashlessUUIDAdapter(MinecraftOAuthProfile.class));
    }

    ProfileCreateRequest(Requester<MinecraftServicesToken> requester, Parser<MinecraftOAuthProfile> parser) {
        super(log, requester, parser);
    }

    private static String profileName(String name) {
        JsonObject object = new JsonObject();
        object.addProperty("profileName", name);
        return new Gson().toJson(object);
    }

    public MinecraftOAuthProfile createProfile(MinecraftServicesToken token) throws MinecraftProfileCreateException, IOException {
        try {
            return requestAndParse(token);
        } catch (InvalidResponseException e) {
            throw new MinecraftProfileCreateException(e);
        }
    }
}
