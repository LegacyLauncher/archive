package ru.turikhay.tlauncher.managers;

import ru.turikhay.tlauncher.minecraft.launcher.ProcessHook;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public interface GPUManager {
    default boolean isEmpty() {
        return false;
    }

    @Nonnull
    List<GPU> discoveryGPUs();

    default Optional<GPU> findGPU(String name) {
        return discoveryGPUs().stream().filter(it -> it.name.equalsIgnoreCase(name)).findAny();
    }

    final class Empty implements GPUManager {
        public static final GPUManager INSTANCE = new Empty();

        private Empty() {
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Nonnull
        @Override
        public List<GPU> discoveryGPUs() {
            return Collections.emptyList();
        }
    }

    final class GPU {
        private final String name;
        private final boolean isDefault;
        private final Supplier<ProcessHook> hook;

        public GPU(String name, boolean isDefault, Supplier<ProcessHook> hook) {
            this.name = name;
            this.isDefault = isDefault;
            this.hook = hook;
        }

        public GPU(String name, boolean isDefault, ProcessHook hook) {
            this(name, isDefault, () -> hook);
        }

        public String getName() {
            return name;
        }

        public boolean isDefault() {
            return isDefault;
        }

        public Supplier<ProcessHook> getHookSupplier() {
            return hook;
        }

        public ProcessHook getHook() {
            return hook.get();
        }
    }
}
