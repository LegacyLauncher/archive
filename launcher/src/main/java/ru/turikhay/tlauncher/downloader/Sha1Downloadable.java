package ru.turikhay.tlauncher.downloader;

import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;

public class Sha1Downloadable extends Downloadable {

    protected String sha1;
    protected long length;

    public Sha1Downloadable() {
    }

    public Sha1Downloadable(Repository repo, String path, File destination, boolean forceDownload, boolean fastDownload) {
        super(repo, path, destination, forceDownload, fastDownload);
    }

    public Sha1Downloadable(Repository repo, String path, File destination, boolean forceDownload) {
        super(repo, path, destination, forceDownload);
    }

    public Sha1Downloadable(Repository repo, String path, File destination) {
        super(repo, path, destination);
    }

    public Sha1Downloadable(String url, File destination) {
        super(url, destination);
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    @Override
    protected void onComplete() throws RetryDownloadException {
        if (length > 0L) {
            BasicFileAttributeView attrs = Files.getFileAttributeView(getDestination().toPath(),
                    BasicFileAttributeView.class);
            long length;
            try {
                length = attrs.readAttributes().size();
            } catch (IOException e) {
                throw new RetryDownloadException("couldn't read file length", e);
            }
            if (this.length != length) {
                throw new RetryDownloadException("file length doesn't match. got: " + length + ", expected: " + this.length);
            }
        }
        if (sha1 != null) {
            String sha1;
            try {
                sha1 = FileUtil.getSha1(getDestination());
            } catch (IOException e) {
                throw new RetryDownloadException("couldn't read sha-1 for downloaded file", e);
            }
            if (!this.sha1.equals(sha1)) {
                throw new RetryDownloadException("illegal library hash. got: " + sha1 + "; expected: " + this.sha1);
            }
        }
        super.onComplete();
    }
}
