package ru.turikhay.util.pastebin;

import java.net.URL;

public abstract class PasteResult {
    private final Paste paste;

    PasteResult(Paste paste) {
        this.paste = paste;
    }

    public final Paste getPaste() {
        return paste;
    }

    public static class PasteFailed extends PasteResult {
        private final Throwable error;

        PasteFailed(Paste paste, Throwable error) {
            super(paste);
            this.error = error;
        }

        public final Throwable getError() {
            return error;
        }

        public String toString() {
            return "PasteFailed{error=\'" + error + "\'}";
        }
    }

    public static class PasteUploaded extends PasteResult {
        private final URL url;

        PasteUploaded(Paste paste, URL url) {
            super(paste);
            this.url = url;
        }

        public final URL getURL() {
            return url;
        }

        public String toString() {
            return "PasteUploaded{url=\'" + url + "\'}";
        }
    }
}
