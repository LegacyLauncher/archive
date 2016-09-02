package ru.turikhay.tlauncher.downloader;

import java.io.IOException;

class GaveUpDownloadException extends IOException {
    private static final long serialVersionUID = 5762388485267411115L;

    GaveUpDownloadException(Downloadable d, Throwable cause) {
        super(d.getURL(), cause);
    }
}
