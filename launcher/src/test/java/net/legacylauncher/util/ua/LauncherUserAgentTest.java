package net.legacylauncher.util.ua;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LauncherUserAgentTest {
    @Test
    void print() {
        System.out.print(LauncherUserAgent.USER_AGENT);
    }
}
