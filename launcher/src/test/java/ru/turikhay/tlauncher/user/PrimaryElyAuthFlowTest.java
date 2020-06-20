package ru.turikhay.tlauncher.user;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URL;

import static org.testng.Assert.*;

public class PrimaryElyAuthFlowTest {
    int state;
    PrimaryElyAuthFlow primaryStrategy;
    PrimaryElyAuthFlow.URIWatchdog watchdog;
    PrimaryElyAuthFlow.HttpServerAdapter server;

    @BeforeMethod
    public void setUp() throws Exception {
        primaryStrategy = new PrimaryElyAuthFlow();
        state = primaryStrategy.generateState();
        watchdog = primaryStrategy.createWatchdog(state);
        server = primaryStrategy.createServer(watchdog);
        primaryStrategy.server = server;
        server.start();
    }

    @AfterMethod
    public void finish() {
        server.stop();
    }

    @Test
    public void testCreateServer() throws Exception {
        final String expectedCode = "xui";

        String response = IOUtils.toString(new URL(
                    String.format(PrimaryElyAuthFlow.SERVER_FULL_URL, server.getPort())
                            + "?" + PrimaryElyAuthFlow.QUERY_CODE_KEY + "=" + expectedCode
                            + "&" + PrimaryElyAuthFlow.QUERY_STATE_KEY + "=" + state)
                .openStream(), "UTF-8");
        ElyAuthCode code = watchdog.waitForCode();

        assertEquals(code.code, expectedCode, "code");
        //assertEquals(response, PrimaryElyAuthFlow.SERVER_RESPONSE, "response");
    }

    @Test
    public void testOpenBrowser() throws Exception {
        primaryStrategy.openBrowser(server.getPort(), state);
        assertNotNull(watchdog.waitForCode());
    }

}