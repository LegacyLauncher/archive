package ru.turikhay.tlauncher.user.minecraft.strategy.preq;

import org.junit.jupiter.api.Test;
import ru.turikhay.tlauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.MockRequester;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MinecraftProfileRequesterTest {

    @Test
    void test() throws MinecraftProfileRequestException, IOException {
        MinecraftProfileRequester s = new MinecraftProfileRequester(
                MockRequester.returning("{\"id\" : \"d9e1d7fea4374d2e819a25745724119e\", \"name\" : \"QWERTY\", \"skins\" : [ ], \"capes\" : [ ]}")
        );
        MinecraftOAuthProfile p = s.requestProfile(new MinecraftServicesToken("token", 0));
        assertEquals(p, new MinecraftOAuthProfile(UUID.fromString("d9e1d7fe-a437-4d2e-819a-25745724119e"), "QWERTY"));
    }

}