package net.legacylauncher.managers;

import net.legacylauncher.minecraft.launcher.hooks.WindowsGpuPreferenceHook;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class WindowsGpuManager implements GPUManager {
    @Nonnull
    @Override
    public List<GPU> discoveryGPUs() {
        return Collections.emptyList();
    }

    @Override
    public Optional<GPU> findDiscreteGPU() {
        return Optional.of(GPU.DISCRETE);
    }

    @Override
    public Optional<GPU> findIntegratedGPU() {
        return Optional.of(GPU.INTEGRATED);
    }

    @Override
    public String toString() {
        return "WindowsGpuManager";
    }

    @Override
    public void close() throws IOException {
    }

    public static Optional<GPUManager> tryToCreate() {
        if (WindowsGpuPreferenceHook.isSupported()) {
            return Optional.of(new WindowsGpuManager());
        }
        return Optional.empty();
    }
}
