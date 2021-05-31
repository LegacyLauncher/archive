package ru.turikhay.tlauncher.jre;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class JavaRuntimeLocalDiscovererTest {

    @Test
    @Disabled
    void realTest() throws IOException {
        JavaRuntimeLocalDiscoverer discoverer = new JavaRuntimeLocalDiscoverer(new File("C:\\Program Files (x86)\\Minecraft Launcher\\runtime"));
        JavaRuntimeLocal runtime = discoverer.getCurrentPlatformRuntime("java-runtime-alpha").get();
        System.out.println(runtime.getVersion());
    }

}