package net.legacylauncher.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SwitcherooControlGPUManagerLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(SwitcherooControlGPUManagerLoader.class);

    public static Optional<GPUManager> tryToCreate() {
        LOGGER.info("SwitcherooControlGPUManager is not available because it requires Java 11+");
        return Optional.empty();
    }
}
