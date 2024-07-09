package net.legacylauncher.user.minecraft.strategy.oatoken;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.user.minecraft.strategy.rqnpr.GsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class GsonParserTest {

    @Test
    void test() {
        String response = "{\"token_type\":\"bearer\",\"expires_in\":86400,\"scope\":\"service::user.auth.xboxlive.com::MBI_SSL\",\"access_token\":\"QWERTY\",\"refresh_token\":\"ASDFG\",\"user_id\":\"1337\",\"foci\":\"1\"}";
        MicrosoftOAuthToken expected = new MicrosoftOAuthToken("QWERTY", "ASDFG", 86400);
        GsonParser<MicrosoftOAuthToken> parser = GsonParser.lowerCaseWithUnderscores(MicrosoftOAuthToken.class);
        assertEquals(parser.parseResponse(log, response), expected);
    }

}