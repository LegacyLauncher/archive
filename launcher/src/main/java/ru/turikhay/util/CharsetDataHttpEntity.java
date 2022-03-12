package ru.turikhay.util;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class CharsetDataHttpEntity extends AbstractHttpEntity {
    private final CharsetData data;
    private final int attempt = 0;

    public CharsetDataHttpEntity(CharsetData data) {
        this.data = Objects.requireNonNull(data, "data");
        setContentType(ContentType.TEXT_PLAIN.withCharset(data.charset()).toString());
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public long getContentLength() {
        return data.length();
    }

    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException {
        return data.stream();
    }

    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        Objects.requireNonNull(outStream, "outStream");
        try (InputStream inputStream = data.stream()) {
            IOUtils.copy(inputStream, outStream);
        }
    }

    @Override
    public boolean isStreaming() {
        return false;
    }
}
