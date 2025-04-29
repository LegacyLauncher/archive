package net.legacylauncher.jre;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.util.OS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
public class JavaPlatform {
    public static final List<String> CURRENT_PLATFORM_CANDIDATES = getCurrentPlatformCandidates();

    private static List<String> getCurrentPlatformCandidates() {
        List<String> candidates = new ArrayList<>();
        switch (OS.CURRENT) {
            case LINUX:
                if (OS.Arch.ARM64.isCurrent()) {
                    candidates.add("linux-arm64");
                }
                if (OS.Arch.IS_64_BIT) {
                    candidates.add("linux");
                } else {
                    candidates.add("linux-i386");
                }
                break;
            case WINDOWS:
                if (OS.Arch.ARM64.isCurrent()) {
                    candidates.add("windows-arm64");
                }
                if (OS.Arch.IS_64_BIT) {
                    candidates.add("windows-x64");
                } else {
                    candidates.add("windows-x86");
                }
                break;
            case OSX:
                if (OS.Arch.ARM64.isCurrent()) {
                    candidates.add("mac-os-arm64");
                }
                candidates.add("mac-os");
                break;
            default:
                log.warn("Current platform is unknown: {} {}", OS.CURRENT, OS.Arch.CURRENT);
                return Collections.emptyList();
        }
        return candidates;
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
