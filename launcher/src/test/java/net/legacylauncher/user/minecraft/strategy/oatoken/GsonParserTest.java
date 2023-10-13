package net.legacylauncher.user.minecraft.strategy.oatoken;

import net.legacylauncher.user.minecraft.strategy.rqnpr.GsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GsonParserTest {
    private static final Logger LOGGER = LogManager.getLogger(GsonParserTest.class);

    @Test
    void test() {
        String response = "{\"token_type\":\"bearer\",\"expires_in\":86400,\"scope\":\"service::user.auth.xboxlive.com::MBI_SSL\",\"access_token\":\"QWERTY\",\"refresh_token\":\"ASDFG\",\"user_id\":\"1337\",\"foci\":\"1\"}";
        MicrosoftOAuthToken expected = new MicrosoftOAuthToken("QWERTY", "ASDFG", 86400);
        GsonParser<MicrosoftOAuthToken> parser = GsonParser.lowerCaseWithUnderscores(MicrosoftOAuthToken.class);
        assertEquals(parser.parseResponse(LOGGER, response), expected);
    }

}