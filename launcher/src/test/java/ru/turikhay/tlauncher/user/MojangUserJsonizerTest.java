package ru.turikhay.tlauncher.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.testng.annotations.Test;
import ru.turikhay.tlauncher.minecraft.auth.UUIDTypeAdapter;
import ru.turikhay.util.U;

import java.util.UUID;

public class MojangUserJsonizerTest {
    private final Gson gson;

    MojangUserJsonizerTest() throws Exception {
        gson = new GsonBuilder()
                .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
                .registerTypeAdapter(MojangUser.class, MojangUser.getJsonizer())
                .create();
    }

    @Test
    public void testSerialize() throws Exception {
        MojangUser user = MojangAuthTest.authorize(new MojangAuth());
        U.log(gson.toJson(user));
    }

    @Test
    public void testDeserialize() throws Exception {
        String text = "{\"accessToken\":\"68b83dde6fae44199a9d1cdf42af9884\",\"userid\":\"00418b79c9fba6d2c74abdf015601ec4\"," +
                "\"uuid\":\"1f52be97476e44c3b3dd8a8be8b3bbb4\",\"displayName\":\"AlexNick02GR\"," +
                "\"clientToken\":\"5513ed60e2cf4552ab8160401bb70a25\",\"username\":\"alex.paokarath4@gmail.com\"}";

        U.log(gson.fromJson(text, MojangUser.class));
    }

}