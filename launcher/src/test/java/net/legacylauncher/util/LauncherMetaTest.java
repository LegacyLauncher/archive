package net.legacylauncher.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.*;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

public class LauncherMetaTest {
    private static final String LAUNCHER_RESOURCES = System.getProperty("net.legacylauncher.test.launcher-resources");

    private static void withLauncherMeta(Consumer<JsonObject> meta) {
        Gson gson = new Gson();
        File file = new File(LAUNCHER_RESOURCES, "META-INF/launcher-meta.json");
        JsonObject jsonObject;
        try (Reader reader = new FileReader(file)) {
            jsonObject = gson.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        meta.accept(jsonObject);
    }

    private static void testLibraryPresents(String artifact) {
        withLauncherMeta(meta -> {
            Assertions.assertTrue(StreamSupport.stream(meta.getAsJsonArray("libraries").spliterator(), false)
                    .map(it -> it.getAsJsonObject().getAsJsonPrimitive("name").getAsString())
                    .anyMatch(name -> name.startsWith(artifact + ":")));
        });
    }

    @Test
    void testNonExistentLibrary() {
        Assertions.assertThrows(AssertionFailedError.class, () -> {
            testLibraryPresents("net.legacylauncher:non-existing-library");
        });
    }

    @Test
    void testCommon() {
        testLibraryPresents("net.legacylauncher:common");
    }

    @Test
    void testUtils() {
        testLibraryPresents("net.legacylauncher:utils");
    }

    @Test
    void testThemeDetector() {
        testLibraryPresents("com.github.Dansoftowner:jSystemThemeDetector");
    }
}
