package ru.turikhay.tlauncher.jre;

import net.minecraft.launcher.updater.DownloadInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tukaani.xz.LZMAInputStream;
import ru.turikhay.util.EHttpClient;
import ru.turikhay.util.FileUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class JavaRuntimeInstallerDirect implements JavaRuntimeInstallerProcess {
    private static final Logger LOGGER = LogManager.getLogger(JavaRuntimeInstallerDirect.class);

    private final JavaRuntimeRemote runtimeInfo;

    private final File runtimeDir;
    private final File workingDir;

    public JavaRuntimeInstallerDirect(File rootDir, JavaRuntimeRemote runtimeInfo) {
        this.runtimeInfo = runtimeInfo;
        this.runtimeDir = runtimeInfo.getRuntimeDir(rootDir);
        this.workingDir = runtimeInfo.getWorkingDir(rootDir);
    }

    private ProgressReporter reporter;
    private List<JavaRuntimeManifest.RuntimeFile> runtimeFiles;
    private List<MissingFile> missingFiles;

    @Override
    public void install(ProgressReporter reporter) throws IOException, InterruptedException {
        this.reporter = reporter;

        if (!workingDir.isDirectory()) {
            LOGGER.debug("Creating working directory: {}", workingDir.getAbsolutePath());
            FileUtil.createFolder(workingDir);
        } else {
            LOGGER.debug("Working directory: {}", workingDir.getAbsolutePath());
        }

        LOGGER.debug("Getting manifest");
        JavaRuntimeManifest manifest;
        try {
            manifest = runtimeInfo.getManifest();
        } catch (ExecutionException e) {
            throw new IOException("Couldn't fetch manifest", e);
        }
        runtimeFiles = manifest.getFiles();

        checkInterrupted();

        LOGGER.debug("Reporting initial progress");
        reporter.reportProgress(0, runtimeFiles.size());

        LOGGER.debug("Ensuring all files are intact");
        missingFiles = listMissingFiles();

        if (missingFiles.isEmpty()) {
            LOGGER.info("Nothing to download. All files are intact.");
        } else {
            LOGGER.info("Will download {} files", missingFiles.size());
            downloadFiles();
            LOGGER.info("Downloaded {} files", missingFiles.size());
        }

        LOGGER.debug("Writing version");
        FileUtils.writeStringToFile(
                new File(runtimeDir, ".version"),
                runtimeInfo.getVersion().getName(),
                StandardCharsets.UTF_8
        );

        LOGGER.info("Installation finished");
        reporter.reportProgress(runtimeFiles.size(), runtimeFiles.size());
    }

    private List<MissingFile> listMissingFiles() throws IOException, InterruptedException {
        List<MissingFile> missingFiles = new ArrayList<>();
        for (JavaRuntimeManifest.RuntimeFile runtimeFile : runtimeFiles) {
            MissingFile missingFile = new MissingFile(runtimeFile);
            if (missingFile.shouldDownload()) {
                missingFiles.add(missingFile);
            }
            checkInterrupted();
        }
        LOGGER.debug("Missing files count: {}", missingFiles.size());
        return missingFiles;
    }

    private HttpClient client;

    private void downloadFiles() throws IOException, InterruptedException {
        try (CloseableHttpClient httpClient = EHttpClient.createRepeatable()) {
            this.client = httpClient;
            for (int i = 0; i < missingFiles.size(); i++) {
                missingFiles.get(i).download();
                reporter.reportProgress(i + 1, missingFiles.size());
                checkInterrupted();
            }
        } finally {
            this.client = null;
        }
    }

    private class MissingFile {
        final JavaRuntimeManifest.RuntimeFile runtimeFile;
        final String path;
        final File realFile;

        MissingFile(JavaRuntimeManifest.RuntimeFile runtimeFile) {
            this.runtimeFile = runtimeFile;
            this.path = runtimeFile.getPath();
            this.realFile = new File(workingDir, path);
        }

        boolean shouldDownload() throws IOException {
            LOGGER.trace("Inspecting runtime entity {}", path);

            if (!runtimeFile.isFile()) {
                LOGGER.debug("Not a file: {}", path);
                return false;
            }

            File realFile = new File(workingDir, path);
            if (!realFile.isFile()) {
                LOGGER.trace("File is missing: {}", path);
                return true;
            }

            int expectedSize = runtimeFile.getDownload().getSize();
            long size = FileUtil.getSize(realFile);

            if (size < 0) {
                LOGGER.warn("Reported negative size ({}): {}", size, realFile.getAbsolutePath());
            } else if (size != expectedSize) {
                LOGGER.info("File {} is corrupted. Expected size {}, but got {}",
                        path, expectedSize, size);
                return true;
            }

            String expectedSha1 = runtimeFile.getDownload().getSha1();
            String sha1 = FileUtil.getSha1(realFile);
            if (!sha1.equalsIgnoreCase(expectedSha1)) {
                LOGGER.info("File {} is corrupted. Expected SHA-1 {}, but got {}",
                        path, expectedSha1, sha1);
                return true;
            }

            LOGGER.trace("File {} is ok", path);
            return false;
        }

        void download() throws IOException, InterruptedException {
            if (runtimeFile.hasLzmaDownload()) {
                LOGGER.debug("Downloading compressed (LZMA) file: {}", runtimeFile.getPath());
                downloadLzma();
            } else {
                LOGGER.debug("Downloading uncompressed file: {}", runtimeFile.getPath());
                downloadRaw();
            }
        }

        private void downloadLzma() throws IOException, InterruptedException {
            File lzmaFile = new File(workingDir, path + ".lzma");
            downloadAndCheck(runtimeFile.getLzmaDownload(), lzmaFile);
            LOGGER.debug("Decompressing: {}", lzmaFile.getAbsolutePath());
            try (LZMAInputStream input =
                         new LZMAInputStream(new BufferedInputStream(new FileInputStream(lzmaFile)));
                 OutputStream output =
                         new BufferedOutputStream(new FileOutputStream(realFile))
            ) {
                IOUtils.copy(input, output);
            } catch (IOException e) {
                throw new IOException(path + ": couldn't decompress the file", e);
            } finally {
                FileUtil.deleteFile(lzmaFile);
            }
            //LOGGER.debug("Checking if the file is decompressed properly: {}", path);
            checkFile(runtimeFile.getDownload(), realFile);
        }

        private void downloadRaw() throws IOException, InterruptedException {
            downloadAndCheck(runtimeFile.getDownload(), realFile);
        }

        private void downloadAndCheck(DownloadInfo downloadInfo, File file)
                throws IOException, InterruptedException {
            LOGGER.trace("Downloading {} into {}", downloadInfo, file.getAbsolutePath());
            HttpGet get = new HttpGet(downloadInfo.getUrl());
            try (
                    InputStream in = client.execute(get).getEntity().getContent();
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(file))
            ) {
                IOUtils.copy(in, out);
            } catch (InterruptedIOException interrupted) {
                throw new InterruptedException();
            }
            checkFile(downloadInfo, file);
            LOGGER.debug("Downloaded successfully: {}", file.getAbsolutePath());
        }

        private void checkFile(DownloadInfo downloadInfo, File file) throws IOException {
            LOGGER.trace("Checking file: {} ({})", file.getAbsolutePath(), downloadInfo.getSha1());
            long size = FileUtil.getSize(file);
            if (size < 0) {
                LOGGER.warn("System reported this file has negative file size" +
                        "({}): {}", size, file.getAbsolutePath());
            } else if (size != downloadInfo.getSize()) {
                throw new IOException(path + ": unexpected file size: " + size + "; expected: " +
                        downloadInfo.getSize());
            }
            String sha1 = FileUtil.getSha1(file);
            if (!downloadInfo.getSha1().equalsIgnoreCase(sha1)) {
                throw new IOException(path + ": bad checksum: " + sha1 + "; expected: " +
                        downloadInfo.getSha1());
            }
        }
    }

    private static void checkInterrupted() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }
}
