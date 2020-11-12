package ru.turikhay.tlauncher.pasta;

import java.net.URL;

public abstract class PastaResult {
    private final Pasta pasta;

    PastaResult(Pasta pasta) {
        this.pasta = pasta;
    }

    public final Pasta getPaste() {
        return pasta;
    }

    public static class PastaFailed extends PastaResult {
        private final Throwable error;

        PastaFailed(Pasta pasta, Throwable error) {
            super(pasta);
            this.error = error;
        }

        public final Throwable getError() {
            return error;
        }

        public String toString() {
            return "PastaFailed{error='" + error + "'}";
        }
    }

    public static class PastaUploaded extends PastaResult {
        private final URL url;

        PastaUploaded(Pasta pasta, URL url) {
            super(pasta);
            this.url = url;
        }

        public final URL getURL() {
            return url;
        }

        public String toString() {
            return "PastaUploaded{url='" + url + "'}";
        }
    }
}
