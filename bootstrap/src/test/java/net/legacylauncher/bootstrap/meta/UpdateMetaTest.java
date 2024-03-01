package net.legacylauncher.bootstrap.meta;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpdateMetaTest {

    @Test
    void realTest() throws Exception {
        UpdateMeta updateMeta = UpdateMeta.fetchFor("legacy", callback -> {}).call();
        assertNotNull(updateMeta);
    }
}