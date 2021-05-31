package ru.turikhay.tlauncher.managers;

import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Version;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.DownloadableContainer;
import ru.turikhay.tlauncher.downloader.DownloadableContainerHandler;
import ru.turikhay.tlauncher.jre.*;
import ru.turikhay.util.OS;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JavaManager {
    private static final Logger LOGGER = LogManager.getLogger(JavaManager.class);
    private static final long EXTRA_SPACE = 64L * 1024L * 1024L;

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
        if(!localRuntimeOpt.isPresent()) {
            return Optional.empty();
        }

        JavaRuntimeLocal localRuntime = localRuntimeOpt.get();
        if(localRuntime.hasOverride()) {
            LOGGER.debug("Local JRE {} has override file, and thus always considered latest",
                    localRuntime.getName());
            return localRuntimeOpt;
        }

        Optional<JavaRuntimeRemote> remoteRuntimeOpt;
        try {
            remoteRuntimeOpt = fetcher.fetch().get(30, TimeUnit.SECONDS).getCurrentPlatformLatestRuntime(name);
        } catch(TimeoutException timeout) {
            LOGGER.warn("Timed out waiting for the remote list. Assuming {} is the latest version", name);
            return localRuntimeOpt;
        } catch (ExecutionException e) {
            LOGGER.warn("Couldn't fetch the remote list. Assuming {} is the latest version", name);
            return localRuntimeOpt;
        }
        if(!remoteRuntimeOpt.isPresent()) {
            LOGGER.warn("Couldn't find {} in the remote list. Assuming it is the latest version", name);
            return localRuntimeOpt;
        }
        JavaRuntimeRemote remoteRuntime = remoteRuntimeOpt.get();

        String localVersion;
        try {
            localVersion = localRuntime.getVersion();
        } catch(IOException ioE) {
            LOGGER.warn("Couldn't read version file of {}. Assuming the local version NOT the latest version", name);
            return Optional.empty();
        }

        String remoteVersion = remoteRuntime.getVersion().getName();
        if(remoteVersion.equals(localVersion)) {
            JavaRuntimeManifest manifest;
            try {
                manifest = remoteRuntime.getManifest();
            } catch (ExecutionException e) {
                LOGGER.debug("Couldn't fetch manifest. Assuming the version {} is latest", name, e);
                return localRuntimeOpt;
            } catch (TimeoutException e) {
                LOGGER.debug("Couldn't fetch manifest in reasonable time. Assuming the version {} is latest", name);
                return localRuntimeOpt;
            }
            File workingDirectory = localRuntime.getWorkingDirectory();
            LOGGER.info("Checking integrity of {}", workingDirectory.getAbsolutePath());
            if(checkIntegrity(manifest, workingDirectory)) {
                return localRuntimeOpt;
            } else {
                LOGGER.info("Local version {} is incomplete or tampered with", name);
                return Optional.empty();
            }
        }

        LOGGER.debug("Local ({}) and remote ({}) versions of {} don't match. Assuming the installed version " +
                "is NOT latest.", localVersion, remoteVersion, name);
        return Optional.empty();
    }

    private boolean checkIntegrity(JavaRuntimeManifest manifest, File workingDirectory) {
        List<FileIntegrityEntry> entries = manifest.toIntegrityEntries();
        return entries.parallelStream().noneMatch(e -> e.isTamperedWithAt(workingDirectory));
    }

    public CompleteVersion.JavaVersion getFallbackRecommendedVersion(Version version) {
        if(
                version.getReleaseTime() != null
                && JavaPlatform.CURRENT_PLATFORM != null
                && OS.JAVA_VERSION.getMajor() > 8
        ) {
            // 21w19a release time (2021-05-12T11:19:15+00:00)
            Instant java16UpgradePoint = Instant.ofEpochSecond(1620818355L);
            // pre-1.17 versions definitely support Java 8, but may not support anything in Java 9 ~ 16
            if (version.getReleaseTime().toInstant().compareTo(java16UpgradePoint) < 0) {
                return new CompleteVersion.JavaVersion("jre-legacy", 8);
            }
        }
        return null;
    }

    public boolean hasEnoughSpaceToInstall(JavaRuntimeRemote remote)
            throws ExecutionException, InterruptedException, TimeoutException {
        long usableSpace = discoverer.getRootDir().getUsableSpace();
        if(usableSpace < 1L) {
            LOGGER.warn("Couldn't query usable space left on {}. Result is {}. Probably has no space left.",
                    discoverer.getRootDir().getAbsolutePath(), usableSpace);
            return false;
        }
        long size = remote.getManifest().countBytes();
        if((size + EXTRA_SPACE) >= usableSpace) {
            LOGGER.warn("Usable space is almost or already exhausted. Need {} + {} bytes," +
                    "but only got {}", size, usableSpace, EXTRA_SPACE);
            return false;
        }
        return true;
    }

    public DownloadableContainer installVersionNow(JavaRuntimeRemote remote, File rootDir, boolean forceDownload)
            throws ExecutionException, InterruptedException, TimeoutException {
        return new JreDownloadableContainer(remote, rootDir, forceDownload);
    }

    private static class JreDownloadableContainer extends DownloadableContainer {
        JreDownloadableContainer(JavaRuntimeRemote remote, File rootDir, boolean forceDownload)
                throws ExecutionException, InterruptedException, TimeoutException {
            addAll(remote.getManifest().toDownloadableList(remote.getWorkingDir(rootDir), forceDownload));
            addHandler(new DownloadableContainerHandler() {
                @Override
                public void onStart(DownloadableContainer var1) {
                }
                @Override
                public void onAbort(DownloadableContainer var1) {
                }
                @Override
                public void onError(DownloadableContainer var1, Downloadable var2, Throwable var3) {
                }
                @Override
                public void onComplete(DownloadableContainer var1, Downloadable var2) {
                }

                @Override
                public void onFullComplete(DownloadableContainer var1) {
                    try {
                        remote.toLocal(rootDir).writeVersion(remote.getVersion().getName());
                    } catch (IOException e) {
                        LOGGER.error("Couldn't write version info", e);
                    }
                }
            });
        }
    }
}
