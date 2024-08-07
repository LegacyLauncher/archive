package net.legacylauncher.managers;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.downloader.DownloadableContainer;
import net.legacylauncher.downloader.DownloadableContainerHandler;
import net.legacylauncher.jre.*;
import net.legacylauncher.util.OS;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Version;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Slf4j
public class JavaManager {
    private static final long EXTRA_SPACE = 64L * 1024L * 1024L;

    // 21w19a release time (2021-05-12T11:19:15+00:00)
    public static final Instant JAVA16_UPGRADE_POINT = Instant.ofEpochSecond(1620818355L);

    private final JavaRuntimeRemoteListFetcher fetcher;
    private final JavaRuntimeLocalDiscoverer discoverer;

    public JavaManager(JavaRuntimeRemoteListFetcher fetcher, JavaRuntimeLocalDiscoverer discoverer) {
        this.fetcher = fetcher;
        this.discoverer = discoverer;
    }

    public JavaManager(File rootDir) {
        this(
                new JavaRuntimeRemoteListFetcher(),
                new JavaRuntimeLocalDiscoverer(rootDir)
        );
    }

    public JavaRuntimeLocalDiscoverer getDiscoverer() {
        return discoverer;
    }

    public JavaRuntimeRemoteListFetcher getFetcher() {
        return fetcher;
    }

    public Optional<JavaRuntimeLocal> getLatestVersionInstalled(String name) throws InterruptedException {
        Optional<JavaRuntimeLocal> localRuntimeOpt = discoverer.getCurrentPlatformRuntime(name);
        if (!localRuntimeOpt.isPresent()) {
            return Optional.empty();
        }

        JavaRuntimeLocal localRuntime = localRuntimeOpt.get();
        if (localRuntime.hasOverride()) {
            log.debug("Local JRE {} has override file, and thus always considered latest",
                    localRuntime.getName());
            return localRuntimeOpt;
        }

        Optional<JavaRuntimeRemote> remoteRuntimeOpt;
        try {
            remoteRuntimeOpt = fetcher.fetch().get().getCurrentPlatformFirstRuntimeCandidate(name);
        } catch (ExecutionException e) {
            log.warn("Couldn't fetch the remote list. Assuming {} is the latest version", name);
            return localRuntimeOpt;
        }
        if (!remoteRuntimeOpt.isPresent()) {
            log.warn("Couldn't find {} in the remote list. Assuming it is the latest version", name);
            return localRuntimeOpt;
        }
        JavaRuntimeRemote remoteRuntime = remoteRuntimeOpt.get();

        String localVersion;
        try {
            localVersion = localRuntime.getVersion();
        } catch (IOException ioE) {
            log.warn("Couldn't read version file of {}. Assuming the local version NOT the latest version", name);
            return Optional.empty();
        }

        String remoteVersion = remoteRuntime.getVersion().getName();
        if (remoteVersion.equals(localVersion)) {
            JavaRuntimeManifest manifest;
            try {
                manifest = remoteRuntime.getManifest();
            } catch (ExecutionException e) {
                log.debug("Couldn't fetch manifest. Assuming the version {} is latest", name, e);
                return localRuntimeOpt;
            }
            File workingDirectory = localRuntime.getWorkingDirectory();
            log.info("Checking integrity of {}", workingDirectory.getAbsolutePath());
            if (checkIntegrity(manifest, workingDirectory)) {
                return localRuntimeOpt;
            } else {
                log.info("Local version {} is incomplete or tampered with", name);
                return Optional.empty();
            }
        }

        log.debug("Local ({}) and remote ({}) versions of {} don't match. Assuming the installed version " +
                "is NOT latest.", localVersion, remoteVersion, name);
        return Optional.empty();
    }

    private boolean checkIntegrity(JavaRuntimeManifest manifest, File workingDirectory) {
        List<FileIntegrityEntry> entries = manifest.toIntegrityEntries();
        return entries.parallelStream().noneMatch(e -> e.isTamperedWithAt(workingDirectory));
    }

    public CompleteVersion.JavaVersion getFallbackRecommendedVersion(Version version, boolean debugging) {
        final String id = version.getID();
        if (JavaPlatform.CURRENT_PLATFORM_CANDIDATES.isEmpty()) {
            if (debugging) {
                log.debug("Current platform is unsupported, and {} won't have fallback JRE", id);
            }
            return null;
        }
        if (version.getReleaseTime() == null) {
            if (debugging) {
                log.debug("Version {} has no release time", id);
            }
            return null;
        }
        if (OS.JAVA_VERSION.getMajor() < 9) {
            if (debugging) {
                log.debug("We're running Java 8; no fallback JRE is required for {}", id);
            }
            return null;
        }
        if (version.getReleaseTime().toInstant().compareTo(JAVA16_UPGRADE_POINT) >= 0) {
            if (debugging) {
                log.debug("Version {} was released after Java 16 upgrade point; no fallback" +
                        " JRE for it.", id);
            }
            return null;
        }
        if (debugging) {
            log.debug("Fallback JRE for {} is {}", id, "jre-legacy");
        }
        return new CompleteVersion.JavaVersion("jre-legacy", 8);
    }

    public boolean hasEnoughSpaceToInstall(JavaRuntimeRemote remote)
            throws ExecutionException, InterruptedException {
        long usableSpace = discoverer.getRootDir().getUsableSpace();
        if (usableSpace < 1L) {
            log.warn("Couldn't query usable space left on {}. Result is {}. Probably has no space left.",
                    discoverer.getRootDir().getAbsolutePath(), usableSpace);
            return false;
        }
        long size = remote.getManifest().countBytes();
        if ((size + EXTRA_SPACE) >= usableSpace) {
            log.warn("Usable space is almost or already exhausted. Need {} + {} bytes," +
                    "but only got {}", size, usableSpace, EXTRA_SPACE);
            return false;
        }
        return true;
    }

    public DownloadableContainer installVersionNow(JavaRuntimeRemote remote, File rootDir, boolean forceDownload)
            throws ExecutionException, InterruptedException {
        return new JreDownloadableContainer(remote, rootDir, forceDownload);
    }

    private static class JreDownloadableContainer extends DownloadableContainer {
        JreDownloadableContainer(JavaRuntimeRemote remote, File rootDir, boolean forceDownload)
                throws ExecutionException, InterruptedException {
            addAll(remote.getManifest().toDownloadableList(remote.getWorkingDir(rootDir), forceDownload));
            addHandler(new DownloadableContainerHandler() {
                @Override
                public void onFullComplete(DownloadableContainer var1) {
                    try {
                        remote.toLocal(rootDir).writeVersion(remote.getVersion().getName());
                    } catch (IOException e) {
                        log.error("Couldn't write version info", e);
                    }
                }
            });
        }
    }
}
