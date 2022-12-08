package ru.turikhay.tlauncher.managers;

import ru.turikhay.tlauncher.minecraft.launcher.hooks.WindowsGpuPreferenceHook;

import javax.annotation.Nonnull;
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

    public static Optional<GPUManager> tryToCreate() {
        if (WindowsGpuPreferenceHook.isSupported()) {
            return Optional.of(new WindowsGpuManager());
        }
        return Optional.empty();
    }
}
