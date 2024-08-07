package net.legacylauncher.jna;

import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.util.Lazy;

import java.util.Optional;

@Slf4j
public class JNAMacOs {
    private static final Lazy<Boolean> IS_UNDER_ROSETTA = Lazy.of(() -> {
        // Based on com.intellij.util.system.CpuArch#isUnderRosetta() (Apache-2.0)
        // https://github.com/JetBrains/intellij-community/blob/3d04f008f7c0cbf2489aa0197879dfe93b12829b/platform/util/src/com/intellij/util/system/CpuArch.java#L75
        IntByReference p = new IntByReference();
        SystemB.size_t.ByReference size = new SystemB.size_t.ByReference(SystemB.INT_SIZE);
        int result = SystemB.INSTANCE.sysctlbyname("sysctl.proc_translated", p.getPointer(), size, null, SystemB.size_t.ZERO);
        if (result == -1) {
            log.warn("Couldn't request \"sysctl.proc_translated\" property");
            return false;
        }
        return p.getValue() == 1;
    });

    public static Optional<Boolean> isUnderRosetta() {
        return JNA.ENABLED ? IS_UNDER_ROSETTA.value() : Optional.empty();
    }
}
