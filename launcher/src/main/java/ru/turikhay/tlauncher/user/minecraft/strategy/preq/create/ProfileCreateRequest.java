package ru.turikhay.tlauncher.user.minecraft.strategy.preq.create;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import ru.turikhay.tlauncher.user.minecraft.strategy.preq.MinecraftOAuthProfile;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.*;

import java.io.IOException;

public class ProfileCreateRequest extends RequestAndParseStrategy<MinecraftServicesToken, MinecraftOAuthProfile> {
    private static final Logger LOGGER = LogManager.getLogger(ProfileCreateRequest.class);

    public ProfileCreateRequest(String name) {
        this(new HttpClientRequester<>(token ->
                Request.Post("https://api.minecraftservices.com/minecraft/profile")
                        .bodyString(profileName(name), ContentType.APPLICATION_JSON)
                        .addHeader("Authorization", "Bearer " + token.getAccessToken()))
        );
    }

    ProfileCreateRequest(Requester<MinecraftServicesToken> requester) {
        this(requester, GsonParser.withDashlessUUIDAdapter(MinecraftOAuthProfile.class));
    }

    ProfileCreateRequest(Requester<MinecraftServicesToken> requester, Parser<MinecraftOAuthProfile> parser) {
        super(LOGGER, requester, parser);
    }

    public MinecraftOAuthProfile createProfile(MinecraftServicesToken token) throws MinecraftProfileCreateException, IOException {
        try {
            return requestAndParse(token);
        } catch (InvalidResponseException e) {
            throw new MinecraftProfileCreateException(e);
        }
    }

    private static String profileName(String name) {
        JsonObject object = new JsonObject();
        object.addProperty("profileName", name);
        return new Gson().toJson(object);
    }
}
