package ru.turikhay.tlauncher.user.minecraft.strategy.mcsauth;

import org.junit.jupiter.api.Test;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.MockRequester;
import ru.turikhay.tlauncher.user.minecraft.strategy.xb.XboxServiceAuthenticationResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MinecraftServicesAuthenticatorTest {

    @Test
    void test() throws MinecraftServicesAuthenticationException, IOException {
        MinecraftServicesAuthenticator s = new MinecraftServicesAuthenticator(
                MockRequester.returning("{\"username\":\"random_uuid\",\"roles\":[],\"access_token\":\"QWERTY\",\"token_type\":\"Bearer\",\"expires_in\":86400}")
        );
        MinecraftServicesToken r = s.minecraftServicesAuthenticate(new XboxServiceAuthenticationResponse("access_token", "1337"));
        assertEquals(r, new MinecraftServicesToken("QWERTY", 86400));
    }

}