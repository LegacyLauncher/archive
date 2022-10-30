package ru.turikhay.tlauncher.managers;

import org.junit.jupiter.api.Test;
import ru.turikhay.tlauncher.managers.MemoryAllocationService.Hint;
import ru.turikhay.tlauncher.managers.MemoryAllocationService.MemoryInfo;
import ru.turikhay.tlauncher.managers.MemoryAllocationService.OsInfo;
import ru.turikhay.tlauncher.managers.MemoryAllocationService.VersionContext;
import ru.turikhay.util.OS;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static ru.turikhay.tlauncher.managers.MemoryAllocationService.DEFAULT_BASE_REQUIREMENT;

class MemoryAllocationServiceTest {

    @Test
    void testSmallMemoryFallbackHint() {
        for(OsInfo os : OS_ALL) {
            MemoryAllocationService service;
            service = new MemoryAllocationService(os, MEM_512);
            assertFallbackHint(512, service);
            service = new MemoryAllocationService(os, MEM_1024);
            assertFallbackHint(1024, service);
            service = new MemoryAllocationService(os, MEM_1536);
            assertFallbackHint(1024, service);
        }
    }

    @Test
    void testX86FallbackHint() {
        for(OsInfo os : OS_ALL_x86) {
            MemoryAllocationService service;
            service = new MemoryAllocationService(os, MEM_512);
            assertFallbackHint(512, service);
            for (int i = 1024; i <= 4096; i += 512) {
                service = new MemoryAllocationService(os, new MemoryInfo(i));
                assertFallbackHint(1024, service);
            }
        }
    }

    @Test
    void testX64FallbackHint() {
        for(OsInfo os : OS_ALL_x64) {
            MemoryAllocationService service;
            service = new MemoryAllocationService(os, MEM_512);
            assertFallbackHint(512, service);
            service = new MemoryAllocationService(os, MEM_1024);
            assertFallbackHint(1024, service);
            service = new MemoryAllocationService(os, MEM_1536);
            assertFallbackHint(1024, service);
            service = new MemoryAllocationService(os, MEM_2048);
            assertFallbackHint(1536, service);
            service = new MemoryAllocationService(os, MEM_4096);
            assertFallbackHint(2048, service);
            for (int i = 4096; i <= 65536; i <<= 1) {
                service = new MemoryAllocationService(os, new MemoryInfo(i));
                assertFallbackHint(2048, service);
            }
        }
    }

    @Test
    void testOldVersionHint() throws ExecutionException, InterruptedException {
        for(OsInfo os : OS_ALL) {
            MemoryAllocationService service;
            service = new MemoryAllocationService(os, MEM_512);
            assertHint(512, 1024, service, OLD_VERSION_CONTEXT);
            assertHint(512, 1024, service, OLD_FORGE_VERSION_CONTEXT);
            assertHint(512, 1024, service, V1_3_CONTEXT);
            assertHint(512, 1024, service, V1_3_FORGE_CONTEXT);
            for (int i = 1024; i <= (os.is64Bit() ? 65536 : 4096); i += 512) {
                service = new MemoryAllocationService(os, new MemoryInfo(i));
                assertHint(1024, 1024, service, OLD_VERSION_CONTEXT);
                assertHint(i >= 6000 ? 2048 : 1024, i >= 6000 ? 2048 : 1024, service, OLD_FORGE_VERSION_CONTEXT);
                assertHint(1024, 1024, service, V1_3_CONTEXT);
                assertHint(i >= 6000 ? 2048 : 1024, i >= 6000 ? 2048 : 1024, service, V1_3_FORGE_CONTEXT);
            }
        }
    }

    @Test
    void testV1_13VersionHint() throws ExecutionException, InterruptedException {
        for(OsInfo os : OS_ALL) {
            MemoryAllocationService service;
            service = new MemoryAllocationService(os, MEM_512);
            assertHint(512, 2048, service, V1_13_CONTEXT);
            assertHint(512, 2048, service, V1_13_FORGE_CONTEXT);
            service = new MemoryAllocationService(os, MEM_1024);
            assertHint(1024, 2048, service, V1_13_CONTEXT);
            assertHint(1024, 2048, service, V1_13_FORGE_CONTEXT);
            service = new MemoryAllocationService(os, MEM_1536);
            assertHint(1024, 2048, service, V1_13_CONTEXT);
            assertHint(1024, 2048, service, V1_13_FORGE_CONTEXT);
            service = new MemoryAllocationService(os, MEM_2048);
            assertHint(os.is64Bit() ? 1536 : 1024, 2048, service, V1_13_CONTEXT);
            assertHint(os.is64Bit() ? 1536 : 1024, 2048, service, V1_13_FORGE_CONTEXT);
            for (int i = 2560; i <= (os.is64Bit() ? 65536 : 4096); i += 512) {
                service = new MemoryAllocationService(os, new MemoryInfo(i));
                assertHint(os.is64Bit() ? 2048 : 1024, 2048, service, V1_13_CONTEXT);
                if (i >= 6000) {
                    assertHint(4096, 4096, service, V1_13_FORGE_CONTEXT);
                } else {
                    assertHint(os.is64Bit() ? 2048 : 1024, 2048, service, V1_13_FORGE_CONTEXT);
                }
            }
        }
    }

    @Test
    void testV1_18VersionHint() throws ExecutionException, InterruptedException {
        for(OsInfo os : OS_ALL) {
            MemoryAllocationService service;
            service = new MemoryAllocationService(os, MEM_512);
            assertHint(512, 2048, service, V1_18_CONTEXT);
            assertHint(512, 2048, service, V1_18_FORGE_CONTEXT);
            service = new MemoryAllocationService(os, MEM_1024);
            assertHint(1024, 2048, service, V1_18_CONTEXT);
            assertHint(1024, 2048, service, V1_18_FORGE_CONTEXT);
            service = new MemoryAllocationService(os, MEM_1536);
            assertHint(1024, 2048, service, V1_18_CONTEXT);
            assertHint(1024, 2048, service, V1_18_FORGE_CONTEXT);
            service = new MemoryAllocationService(os, MEM_2048);
            assertHint(os.is64Bit() ? 1536 : 1024, 2048, service, V1_18_CONTEXT);
            assertHint(os.is64Bit() ? 1536 : 1024, 2048, service, V1_18_FORGE_CONTEXT);
            service = new MemoryAllocationService(os, MEM_3072);
            assertHint(os.is64Bit() ? 2048 : 1024, 2048, service, V1_18_CONTEXT);
            assertHint(os.is64Bit() ? 2048 : 1024, 2048, service, V1_18_FORGE_CONTEXT);
            service = new MemoryAllocationService(os, MEM_4096);
            assertHint(os.is64Bit() ? os.isArm() ? 2048 : 3072 : 1024, os.is64Bit() ? os.isArm() ? 2048 : 3072 : 3072, service, V1_18_CONTEXT);
            assertHint(os.is64Bit() ? os.isArm() ? 2048 : 3072 : 1024, os.is64Bit() ? os.isArm() ? 2048 : 3072 : 3072, service, V1_18_FORGE_CONTEXT);
            if (os.is64Bit()) {
                service = new MemoryAllocationService(os, MEM_8192);
                assertHint(os.isArm() ? 2048 : 4096, os.isArm() ? 2048 : 4096, service, V1_18_CONTEXT);
                assertHint(os.isArm() ? 4096 : 6144, os.isArm() ? 4096 : 6144, service, V1_18_FORGE_CONTEXT);
                service = new MemoryAllocationService(os, MEM_16384);
                assertHint(4096, 4096, service, V1_18_CONTEXT);
                assertHint(8192, 8192, service, V1_18_FORGE_CONTEXT);
            }
        }
    }

    static void assertFallbackHint(int actual, MemoryAllocationService service) {
        assertEquals(
                new Hint(actual, DEFAULT_BASE_REQUIREMENT, false),
                service.getFallbackHint(),
                service.toString()
        );
    }

    static void assertHint(int actual, int desired, MemoryAllocationService service, VersionContext versionContext)
            throws ExecutionException, InterruptedException {
        assertEquals(
                new Hint(actual, desired, true),
                service.queryHint(versionContext).get(),
                versionContext + "\n" + service
        );
    }

    static final OsInfo
            WINDOWS_x64 = new OsInfo(OS.WINDOWS, OS.Arch.x64),
            WINDOWS_x86 = new OsInfo(OS.WINDOWS, OS.Arch.x86),
            LINUX_x64 = new OsInfo(OS.LINUX, OS.Arch.x64),
            LINUX_x86 = new OsInfo(OS.LINUX, OS.Arch.x86),
            MACOS_x64 = new OsInfo(OS.OSX, OS.Arch.x64),
            MACOS_ARM64 = new OsInfo(OS.OSX, OS.Arch.ARM64);

    static final List<OsInfo> WINDOWS = List.of(WINDOWS_x64, WINDOWS_x86);
    static final List<OsInfo> LINUX = List.of(LINUX_x64, LINUX_x86);
    static final List<OsInfo> MACOS = List.of(MACOS_x64, MACOS_ARM64);
    static final List<OsInfo> OS_ALL = Stream.of(WINDOWS, LINUX, MACOS)
            .flatMap(List::stream)
            .collect(Collectors.toList());

    static final List<OsInfo> OS_ALL_x86 = OS_ALL
            .stream().filter(os -> os.isArch(OS.Arch.x86))
            .collect(Collectors.toList());

    static final List<OsInfo> OS_ALL_x64 = OS_ALL
            .stream().filter(os -> os.isArch(OS.Arch.x64))
            .collect(Collectors.toList());

    static final MemoryInfo MEM_512 = new MemoryInfo(512);
    static final MemoryInfo MEM_1024 = new MemoryInfo(1024);
    static final MemoryInfo MEM_1536 = new MemoryInfo(1536);
    static final MemoryInfo MEM_2048 = new MemoryInfo(2048);
    static final MemoryInfo MEM_3072 = new MemoryInfo(3072);
    static final MemoryInfo MEM_4096 = new MemoryInfo(4096);
    static final MemoryInfo MEM_8192 = new MemoryInfo(8192);
    static final MemoryInfo MEM_16384 = new MemoryInfo(16384);

    static final VersionContext OLD_VERSION_CONTEXT =
            versionContext("old", "2009-05-13T20:11:00+00:00");

    static final VersionContext OLD_FORGE_VERSION_CONTEXT =
            versionContext("old-forge", "2009-05-13T20:11:00+00:00");

    static final VersionContext V1_3_CONTEXT =
            versionContext("1.3", "2012-07-25T22:00:00+00:00");

    static final VersionContext V1_3_FORGE_CONTEXT =
            versionContext("forge-1.3", "2012-07-25T22:00:00+00:00");

    static final VersionContext V1_13_CONTEXT =
            versionContext("1.13", "2018-02-14T17:34:13+00:00");

    static final VersionContext V1_13_FORGE_CONTEXT =
            versionContext("forge-1.13", "2018-02-14T17:34:13+00:00");

    static final VersionContext V1_18_CONTEXT =
            versionContext("1.18", "2021-07-13T12:54:19+00:00");

    static final VersionContext V1_18_FORGE_CONTEXT =
            versionContext("forge-1.18", "2021-07-13T12:54:19+00:00");

    static VersionContext versionContext(String id, String releaseTime) {
        return new VersionContext(id, BaseVersionTable.parseDate(releaseTime), null);
    }

}