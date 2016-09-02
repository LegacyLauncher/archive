package ru.turikhay.tlauncher.bootstrap.meta;

import java.net.URL;

public class RemoteBootstrapMeta extends BootstrapMeta {
    private String checksum;
    private URL download;

    public String getChecksum() {
        return checksum;
    }

    public URL getDownload() {
        return download;
    }
}
