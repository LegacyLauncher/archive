package ru.turikhay.tlauncher.bootstrap.task;

import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class DownloadTask extends Task<Void> {
    private final URL url;
    private final File file;
    private final String sha256;

    public DownloadTask(URL url, File file, String sha256) {
        super("Download:" + url);

        this.url = url;
        this.file = file;
        this.sha256 = sha256;
    }

    @Override
    protected Void execute() throws Exception {
        updateProgress(-1.);

        log("Opening connection:", url);
        URLConnection connection = url.openConnection(U.getProxy());
        InputStream in = null;
        OutputStream out = null;

        try {
            in = connection.getInputStream();
            out = new FileOutputStream(file);

            byte[] buffer = new byte[2048];
            long read = 0L;
            int i;

            while ((i = in.read(buffer)) != -1) {
                out.write(buffer, 0, i);

                read += i;

                if (connection.getContentLengthLong() > 0) {
                    updateProgress((double) (read / connection.getContentLengthLong()));
                }
            }

            log("Downloaded", read, " bytes out of", connection.getContentLengthLong());

            checkSha256:
            {
                if (sha256 != null) {
                    log("Checking SHA256... Expected:", sha256);
                    out.close();

                    String gotSha256 = U.getSHA256(file);
                    log("Got:", gotSha256);

                    if (sha256.equalsIgnoreCase(gotSha256)) {
                        break checkSha256;
                    }

                    log("Invalid checksum");
                    throw new IOException("invalid checksum. expected: " + sha256 + "; got: " + gotSha256);
                }
            }

            log("Downloaded successfully");
        } finally {
            U.close(in, out);
        }
        return null;
    }
}
