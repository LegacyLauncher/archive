package ru.turikhay.tlauncher.user.minecraft.strategy.oareq;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OAuthUrlParserTest {

    private final OAuthUrlParser p = new OAuthUrlParser();

    @Test
    void fullUrlTest() throws MicrosoftOAuthCodeRequestException {
        String r = p.parseAndValidate("https://login.live.com/oauth20_desktop.srf?code=M.R3_BAY.0204d3ba-ee88-4985-a70f-98a20fe4351e&lc=1049");
        assertEquals(r, "M.R3_BAY.0204d3ba-ee88-4985-a70f-98a20fe4351e");
    }

    @Test
    void onlyPathTest() throws MicrosoftOAuthCodeRequestException {
        String r = p.parseAndValidate("/?code=M.R3_BAY.0204d3ba-ee88-4985-a70f-98a20fe4351e&lc=1049");
        assertEquals(r, "M.R3_BAY.0204d3ba-ee88-4985-a70f-98a20fe4351e");
    }

    @Test
    void cancelledTest() {
        assertThrows(MicrosoftOAuthCodeRequestException.class, () -> p.parseAndValidate("https://login.live.com/oauth20_desktop.srf?error=access_denied&error_description=The%20user%20has%20denied%20access%20to%20the%20scope%20requested%20by%20the%20client%20application.&lc=1033"));
    }

}