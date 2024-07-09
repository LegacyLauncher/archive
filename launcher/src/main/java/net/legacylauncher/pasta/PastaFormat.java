package net.legacylauncher.pasta;


import lombok.Getter;
import org.apache.hc.core5.http.ContentType;

import java.util.Locale;

@Getter
public enum PastaFormat {
    LOGS(ContentType.create("text/x-ansi")),
    JSON(ContentType.APPLICATION_JSON),
    XML(ContentType.APPLICATION_XML),
    PLAIN(ContentType.TEXT_PLAIN);

    private final ContentType contentType;

    PastaFormat(ContentType contentType) {
        this.contentType = contentType;
    }

    String value() {
        return name().toLowerCase(Locale.ROOT);
    }
}
