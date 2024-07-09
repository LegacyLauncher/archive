package net.legacylauncher.util.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.fluent.*;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.ByteArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

@Slf4j
public class RetryingRangeContentResponseHandler extends ContentResponseHandler {
    static final int BUFFER_SIZE = 4096;

    private final Request request;
    private final Executor executor;

    public RetryingRangeContentResponseHandler(Request request, Executor executor) {
        this.request = request;
        this.executor = executor;
    }

    @Override
    public Content handleEntity(HttpEntity entity) throws IOException {
        if (entity == null) {
            return super.handleEntity(null);
        }

        long length = entity.getContentLength();
        InputStream content = entity.getContent();
        ByteArrayBuffer buffer = new ByteArrayBuffer(length > 0 ? (int) length : BUFFER_SIZE);

        long read = readBuffer(content, buffer);

        if (length > 0 && read < length) {
            log.warn("Premature EOF while downloading {} ({} / {})",
                    request, read, length);
            EntityUtils.consume(entity);
            log.warn("Enabling retrying range download");
            while (read < length) {
                log.debug("Downloading range {}: {} / {}",
                        request, read, length);
                RangeResponseHandler handler = new RangeResponseHandler(read, buffer);
                request.setHeader("Range", String.format(Locale.ROOT,
                        "bytes=%d-%d", read, length));
                Response response = executor.execute(request);
                response.handleResponse(handler);
                read = handler.totalRead;
            }
            log.debug("Done range downloading {}: {} / {}", request, read, length);
        }
        ContentType contentType = ContentType.parseLenient(entity.getContentType());
        if (contentType == null) {
            contentType = ContentType.TEXT_PLAIN;
        }
        return new Content(buffer.toByteArray(), contentType);
    }

    long readBuffer(InputStream content, ByteArrayBuffer buffer) throws IOException {
        final byte[] tmp = new byte[BUFFER_SIZE];
        long l = 0L;
        int r;
        while ((r = content.read(tmp)) != -1) {
            buffer.append(tmp, 0, r);
            l += r;
        }
        return l;
    }

    private class RangeResponseHandler extends AbstractHttpClientResponseHandler<Void> {
        private final long start;
        private final ByteArrayBuffer buffer;
        long totalRead;

        RangeResponseHandler(long start, ByteArrayBuffer buffer) {
            this.start = start;
            this.buffer = buffer;
        }

        private void handle(HttpEntity entity) throws IOException {
            Objects.requireNonNull(entity, "entity");
            long read = readBuffer(entity.getContent(), buffer);
            totalRead = start + read;
        }

        @Override
        public Void handleResponse(final ClassicHttpResponse response) throws IOException {
            int statusCode = response.getCode();
            if (statusCode == 200) {
                throw new HttpResponseException(200, "Expected partial download response");
            } else if (statusCode != 206) {
                throw new HttpResponseException(statusCode, response.getReasonPhrase());
            }
            return handleEntity(response.getEntity());
        }

        @Override
        public Void handleEntity(HttpEntity entity) throws IOException {
            handle(entity);
            return null;
        }
    }
}
