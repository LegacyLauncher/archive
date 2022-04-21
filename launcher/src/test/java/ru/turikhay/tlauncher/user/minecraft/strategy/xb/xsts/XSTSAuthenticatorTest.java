package ru.turikhay.tlauncher.user.minecraft.strategy.xb.xsts;

import org.junit.jupiter.api.Test;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.InvalidStatusCodeException;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.MockRequester;
import ru.turikhay.tlauncher.user.minecraft.strategy.xb.XboxServiceAuthenticationResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class XSTSAuthenticatorTest {
    @Test
    void test() throws XSTSAuthenticationException, IOException {
        XSTSAuthenticator s = new XSTSAuthenticator(
                MockRequester.returning("{\"IssueInstant\":\"2020-12-11T13:50:47.2400323Z\",\"NotAfter\":\"2020-12-12T05:50:47.2400323Z\",\"Token\":\"QWERTY\",\"DisplayClaims\":{\"xui\":[{\"uhs\":\"1337\"}]}}")
        );
        XboxServiceAuthenticationResponse r = s.xstsAuthenticate("access_token");
        assertEquals(r, new XboxServiceAuthenticationResponse("QWERTY", "1337"));
    }

    @Test
    void testNoXbox() {
        XSTSAuthenticator s = new XSTSAuthenticator(
                MockRequester.throwing(
                        new InvalidStatusCodeException(
                                401,
                                "{\"Identity\":\"0\",\"XErr\":2148916233,\"Message\":\"\"}"
                        )
                )
        );
        assertThrows(NoXboxAccountException.class, () -> s.xstsAuthenticate("access_token"));
    }

    @Test
    void testChild() {
        XSTSAuthenticator s = new XSTSAuthenticator(
                MockRequester.throwing(
                        new InvalidStatusCodeException(
                                401,
                                "{\"Identity\":\"0\",\"XErr\":2148916238,\"Message\":\"\",\"Redirect\":\"https://start.ui.xboxlive.com/AddChildToFamily\"}"
                        )
                )
        );
        assertThrows(ChildAccountException.class, () -> s.xstsAuthenticate("access_token"));
    }
}