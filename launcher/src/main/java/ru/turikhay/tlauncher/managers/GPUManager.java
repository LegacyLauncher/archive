package ru.turikhay.tlauncher.managers;

import ru.turikhay.tlauncher.minecraft.launcher.ProcessHook;
import ru.turikhay.tlauncher.minecraft.launcher.hooks.WindowsGpuPreferenceHook;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.util.OS;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface GPUManager {
    default boolean isEmpty() {
        return false;
    }

    @Nonnull
    List<GPU> discoveryGPUs();

    default Optional<GPU> findGPU(String name) {
        return discoveryGPUs().stream().filter(it -> it.name.equalsIgnoreCase(name)).findAny();
    }

    default Optional<GPU> findIntegratedGPU() {
        return discoveryGPUs().stream().filter(it -> it.isDefault).findAny();
    }

    default Optional<GPU> findDiscreteGPU() {
        return discoveryGPUs().stream().filter(it -> !it.isDefault).findAny();
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

        @Override
        public Optional<GPU> findGPU(String name) {
            return Optional.empty();
        }

        @Override
        public Optional<GPU> findIntegratedGPU() {
            return Optional.empty();
        }

        @Override
        public Optional<GPU> findDiscreteGPU() {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return "Empty";
        }
    }

    class GPU {
        private final String name;
        private final Vendor vendor;
        private final boolean isDefault;
        private final Function<GPUManager, ProcessHook> hook;

        public static final GPU DEFAULT = new GPU("SYSTEM", Vendor.Unknown, false, ProcessHook.None.INSTANCE) {
            @Override
            public String getDisplayName(GPUManager gpuManager) {
                return Localizable.get("settings.gpu.default.label");
            }
        };
        public static final GPU INTEGRATED, DISCRETE;

        static {
            if (OS.WINDOWS.isCurrent()) {
                INTEGRATED = new GPU("INTEGRATED", Vendor.Unknown, false,
                        new WindowsGpuPreferenceHook(WindowsGpuPreferenceHook.Preference.MinimumPower)) {
                    @Override
                    public String getDisplayName(GPUManager gpuManager) {
                        return Localizable.get("settings.gpu.integrated.label");
                    }
                };
                DISCRETE = new GPU("DISCRETE", Vendor.Unknown, false,
                        new WindowsGpuPreferenceHook(WindowsGpuPreferenceHook.Preference.HighPerformance)) {
                    @Override
                    public String getDisplayName(GPUManager gpuManager) {
                        return Localizable.get("settings.gpu.discrete.label");
                    }
                };
            } else {
                INTEGRATED = new GPU("INTEGRATED", Vendor.Unknown, false,
                        (manager) -> manager.findIntegratedGPU().map(gpu -> gpu.getHook(manager)).orElse(ProcessHook.None.INSTANCE)) {
                    @Override
                    public String getDisplayName(GPUManager gpuManager) {
                        return gpuManager.findIntegratedGPU().map(it -> Localizable.get("settings.gpu.integrated.specific.label", it.getDisplayName(gpuManager)))
                                .orElse(Localizable.get("settings.gpu.integrated.label"));
                    }
                };
                DISCRETE = new GPU("DISCRETE", Vendor.Unknown, false,
                        (manager) -> manager.findDiscreteGPU().map(gpu -> gpu.getHook(manager)).orElse(ProcessHook.None.INSTANCE)) {
                    @Override
                    public String getDisplayName(GPUManager gpuManager) {
                        return gpuManager.findIntegratedGPU().map(it -> Localizable.get("settings.gpu.discrete.specific.label", it.getDisplayName(gpuManager)))
                                .orElse(Localizable.get("settings.gpu.discrete.label"));
                    }
                };
            }
        }

        public static final List<GPU> GLOBAL_DEFINED = Collections.unmodifiableList(Arrays.asList(DEFAULT, INTEGRATED, DISCRETE));

        public GPU(String name, Vendor vendor, boolean isDefault, Function<GPUManager, ProcessHook> hook) {
            this.name = name;
            this.vendor = vendor;
            this.isDefault = isDefault;
            this.hook = hook;
        }

        public GPU(String name, Vendor vendor, boolean isDefault, Supplier<ProcessHook> hook) {
            this(name, vendor, isDefault, (manager) -> hook.get());
        }

        public GPU(String name, Vendor vendor, boolean isDefault, ProcessHook hook) {
            this(name, vendor, isDefault, () -> hook);
        }

        public String getName() {
            return name;
        }

        public String getDisplayName(GPUManager gpuManager) {
            return getName();
        }

        public Vendor getVendor() {
            return vendor;
        }

        public boolean isDefault() {
            return isDefault;
        }

        public ProcessHook getHook(GPUManager gpuManager) {
            return hook.apply(gpuManager);
        }
    }

    public enum Vendor {
        Unknown,
        AMD,
        Intel,
        Nvidia,
        ;

        private static final Map<Vendor, Collection<String>> KEYWORDS = new EnumMap<>(Vendor.class);

        static {
            // keep it lowercase
            KEYWORDS.put(AMD, Arrays.asList("amd", "radeon", "vega"));
            KEYWORDS.put(Intel, Arrays.asList("intel", "uhd"));
            KEYWORDS.put(Nvidia, Arrays.asList("nvidia", "geforce", "gtx", "rtx"));
        }

        public static Vendor guessVendorByName(String name) {
            String nameLowercase = name.toLowerCase(Locale.ROOT);
            for (Map.Entry<Vendor, Collection<String>> entry : KEYWORDS.entrySet()) {
                if (entry.getValue().stream().anyMatch(nameLowercase::contains)) {
                    return entry.getKey();
                }
            }
            return Vendor.Unknown;
        }
    }
}
