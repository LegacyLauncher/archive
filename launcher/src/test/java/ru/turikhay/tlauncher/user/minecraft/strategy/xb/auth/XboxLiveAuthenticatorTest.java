package ru.turikhay.tlauncher.user.minecraft.strategy.xb.auth;

import org.junit.jupiter.api.Test;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.MockRequester;
import ru.turikhay.tlauncher.user.minecraft.strategy.xb.XboxServiceAuthenticationResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XboxLiveAuthenticatorTest {
    @Test
    void test() throws XboxLiveAuthenticationException, IOException {
        XboxLiveAuthenticator s = new XboxLiveAuthenticator(
                MockRequester.returning("{\"IssueInstant\":\"2020-12-11T13:31:13.4667211Z\",\"NotAfter\":\"2020-12-25T13:31:13.4667211Z\",\"Token\":\"QWERTY\",\"DisplayClaims\":{\"xui\":[{\"uhs\":\"1337\"}]}}")
        );
        XboxServiceAuthenticationResponse r = s.xboxLiveAuthenticate("access_token");
        assertEquals(r, new XboxServiceAuthenticationResponse("QWERTY", "1337"));
    }

}