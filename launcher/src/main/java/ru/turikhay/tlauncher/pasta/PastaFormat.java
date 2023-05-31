package ru.turikhay.tlauncher.pasta;

import org.apache.http.entity.ContentType;

import java.util.Locale;

public enum PastaFormat {
    LOGS(ContentType.create("text/x-ansi")),
    JSON(ContentType.APPLICATION_JSON),
    XML(ContentType.APPLICATION_XML),
    PLAIN(ContentType.TEXT_PLAIN);

    private final ContentType contentType;

    PastaFormat(ContentType contentType) {
        this.contentType = contentType;
    }

    public ContentType getContentType() {
        return contentType;
    }

    String value() {
        return name().toLowerCase(Locale.ROOT);
    }
}
