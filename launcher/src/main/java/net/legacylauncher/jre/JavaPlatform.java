package net.legacylauncher.jre;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.util.OS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static net.legacylauncher.util.OS.Arch.IS_64_BIT;

@Slf4j
public class JavaPlatform {
    public static final List<String> CURRENT_PLATFORM_CANDIDATES = getCurrentPlatformCandidates();

    private static List<String> getCurrentPlatformCandidates() {
        switch (OS.CURRENT) {
            case LINUX:
                return Collections.singletonList(IS_64_BIT ? "linux" : "linux-i386");
            case WINDOWS:
                return Collections.singletonList(IS_64_BIT ? "windows-x64" : "windows-x86");
            case OSX:
                List<String> macOsPlatforms = new ArrayList<>();
                if (OS.Arch.ARM64.isCurrent()) {
                    macOsPlatforms.add("mac-os-arm64");
                }
                macOsPlatforms.add("mac-os");
                return Collections.unmodifiableList(macOsPlatforms);
            default:
                log.warn("Current platform is unknown: {} {}", OS.CURRENT, OS.Arch.CURRENT);
                return Collections.emptyList();
        }
    }

    public static OS getOSByPlatform(String platform) {
        Objects.requireNonNull(platform, "platform");

        if (platform.startsWith("linux")) {
            return OS.LINUX;
        } else if (platform.startsWith("windows")) {
            return OS.WINDOWS;
        } else if (platform.startsWith("mac-os")) {
            return OS.OSX;
        } else {
            throw new IllegalArgumentException("unknown platform: " + platform);
        }
    }
}
