package net.legacylauncher.user.minecraft.strategy.preq.create;

import net.legacylauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import net.legacylauncher.user.minecraft.strategy.preq.MinecraftOAuthProfile;
import net.legacylauncher.user.minecraft.strategy.rqnpr.MockRequester;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProfileCreateRequestTest {

    @Test
    void test() throws IOException, MinecraftProfileCreateException {
        ProfileCreateRequest s = new ProfileCreateRequest(
                MockRequester.returning("{\"id\" : \"d9e1d7fea4374d2e819a25745724119e\", \"name\" : \"QWERTY\", \"skins\" : [ ], \"capes\" : [ ]}")
        );
        MinecraftOAuthProfile p = s.createProfile(new MinecraftServicesToken("token", 0));
        assertEquals(p, new MinecraftOAuthProfile(UUID.fromString("d9e1d7fe-a437-4d2e-819a-25745724119e"), "QWERTY"));
    }
}
