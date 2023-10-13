package net.legacylauncher.minecraft.launcher.hooks;

import net.legacylauncher.minecraft.launcher.ProcessHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class GameModeHookLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameModeHookLoader.class);

    public static Optional<ProcessHook> tryToCreate() {
        LOGGER.info("GameModeHook is not available because it requires Java 11+");
        return Optional.empty();
    }

    public static boolean isAvailable() {
        return false;
    }
}
