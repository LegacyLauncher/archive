package ru.turikhay.tlauncher.bootstrap.task;

import ru.turikhay.tlauncher.bootstrap.Bootstrap;
import ru.turikhay.tlauncher.bootstrap.exception.ExceptionList;
import ru.turikhay.tlauncher.bootstrap.exception.FileLockedException;
import ru.turikhay.tlauncher.bootstrap.util.DataBuilder;
import ru.turikhay.tlauncher.bootstrap.util.U;
import shaded.org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DownloadTask extends Task<Void> {
    private final List<URL> urlList;
    private final File file;
    private final String sha256;

    public DownloadTask(String name, List<URL> urlList, File file, String sha256) {
        super(name);

        if(U.requireNotNull(urlList, "urlList").isEmpty()) {
            throw new IllegalArgumentException("url list is empty");
        }

        this.urlList = new ArrayList<URL>(urlList);
        this.file = file;
        this.sha256 = sha256;
    }

    public DownloadTask(String name, URL url, File file, String sha256) {
        this(name, Collections.singletonList(url), file, sha256);
    }

    @Override
    protected Void execute() throws Exception {
        updateProgress(-1.);

        if(file.isFile() && sha256 != null) {
            log("File exists, checking checksum: ", sha256);
            String hash = U.getSHA256(file);
            if(sha256.equalsIgnoreCase(hash)) {
                log("File is the same. Download skipped.");
                return null;
            } else {
                log("File might be corrupted: ", hash);
                Bootstrap.recordBreadcrumb(DownloadTask.class, "file_corrupted", DataBuilder.create("file", file.getAbsolutePath()).add("expected_hash", sha256).add("got_hash", hash));
            }
        }

        List<Exception> ioEList = new ArrayList<Exception>();

        for (URL url : urlList) {
            try {
                downloadUrl(url);
            } catch(TaskInterruptedException interrupted) {
                throw interrupted;
            } catch(FileLockedException locked) {
                log("File is locked:", locked);
                throw locked;
            } catch (IOException ioE) {
                log("Failed to download:", url, ioE);
                ioEList.add(ioE);

                Bootstrap.recordBreadcrumbError(DownloadTask.class, "error", ioE, DataBuilder.create("url", url).add("sha256", sha256));
                continue;
            }
            return null;
        }
        throw new ExceptionList(ioEList);
    }

    protected void downloadUrl(URL url) throws IOException, TaskInterruptedException {
        log("Downloading:", url);

        URLConnection connection = url.openConnection(U.getProxy());
        InputStream in = null;
        OutputStream out = null;
        double contentLength = 0.;

        try {
            File temp = File.createTempFile("tlauncher", null);
            temp.deleteOnExit();

            in = connection.getInputStream();
            out = new FileOutputStream(temp);

            byte[] buffer = new byte[2048];
            long read = 0L;
            int i;

            contentLength = (double) connection.getContentLengthLong();

            while ((i = in.read(buffer)) != -1) {
                out.write(buffer, 0, i);

                read += i;

                if (contentLength != 0.) {
                    updateProgress(read / contentLength);
                }

                checkInterrupted();
            }

            log("Downloaded", read, " bytes out of", connection.getContentLengthLong());

            checkSha256:
            {
                if (sha256 != null) {
                    log("Checking SHA256... Expected: ", sha256);
                    out.close();

                    String gotSha256 = U.getSHA256(temp);
                    log("Got: ", gotSha256);

                    if (sha256.equalsIgnoreCase(gotSha256)) {
                        break checkSha256;
                    }

                    log("Invalid checksum");
                    throw new IOException("invalid checksum. expected: " + sha256 + "; got: " + gotSha256);
                }
            }

            log("Downloaded successfully, copying back...");

            out.close();

            boolean tryCopy = false;
            do {
                try {
                    FileUtils.copyFile(temp, file);
                } catch(FileNotFoundException fnf) {
                    FileLockedException locked = FileLockedException.getIfPresent(fnf);

                    if(tryCopy) { // already tried
                        throw locked == null? fnf : locked;
                    }

                    if(locked != null) {
                        log("We got the situation! File is locked. Let's wait some time...", locked);
                    } else {
                        log("File is probably locked, waiting:", fnf, temp, file);
                    }

                    try {
                        Thread.sleep(FileLockedException.LOCK_COOLDOWN);
                    } catch(InterruptedException interrupted) {
                        throw new TaskInterruptedException(this);
                    }

                    tryCopy = true; // let's try again
                }
            } while(tryCopy);

            Bootstrap.recordBreadcrumb(DownloadTask.class, "success", DataBuilder.create("url", url).add("sha256", sha256));
        } finally {
            U.close(in, out);
        }
    }
}
