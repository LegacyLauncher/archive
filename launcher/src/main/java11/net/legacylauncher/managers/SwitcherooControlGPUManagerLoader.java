package net.legacylauncher.managers;

import java.util.Optional;

@SuppressWarnings("unused") // multi-release override
public class SwitcherooControlGPUManagerLoader {
    public static Optional<GPUManager> tryToCreate() {
        return SwitcherooControlGPUManager.tryToCreate();
    }
}
