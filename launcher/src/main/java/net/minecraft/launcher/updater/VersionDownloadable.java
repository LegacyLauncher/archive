package net.minecraft.launcher.updater;

import net.legacylauncher.common.exceptions.LocalIOException;
import net.legacylauncher.downloader.Downloadable;
import net.legacylauncher.downloader.RetryDownloadException;
import net.legacylauncher.repository.Repository;
import net.legacylauncher.util.FileUtil;
import net.minecraft.launcher.versions.CompleteVersion;

import java.io.File;

public class VersionDownloadable extends Downloadable {

    private final CompleteVersion version;

    public VersionDownloadable(CompleteVersion version, File destination, Repository repository) {
        this.version = version;

        setDestination(destination);
        addAdditionalDestination(new File(destination.getAbsolutePath() + ".bak"));

        if (version.getDownloadURL(DownloadType.CLIENT) != null) {
            DownloadInfo downloadInfo = version.getDownloadURL(DownloadType.CLIENT);
            setURL(Repository.PROXIFIED_REPO, downloadInfo.getUrl());
        } else {
            Repository repo;
            String id;

            if (version.getJar() == null) {
                repo = repository == null ? Repository.OFFICIAL_VERSION_REPO : repository;
                id = version.getID();
            } else {
                repo = Repository.OFFICIAL_VERSION_REPO;
                id = version.getJar();
            }

            String path = "versions/" + id + "/" + id + ".jar";
            setURL(repo, path);
        }
    }

    public void onComplete() throws RetryDownloadException, LocalIOException {
        DownloadInfo downloadInfo = version.getDownloadURL(DownloadType.CLIENT);

        if (downloadInfo == null) {
            return;
        }

        File destination = getDestination();

        if (destination.length() != downloadInfo.getSize()) {
            throw new RetryDownloadException("file size mismatch");
        }

        String hash = FileUtil.getSHA(destination);

        if (!downloadInfo.getSha1().equals(hash)) {
            throw new RetryDownloadException("hash mismatch, got: " + hash + ", expected: " + downloadInfo.getSha1());
        }
    }
}
