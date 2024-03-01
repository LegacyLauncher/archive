package net.legacylauncher.util.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.fluent.*;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

public class RetryingRangeContentResponseHandler extends ContentResponseHandler {
    private static final Logger LOGGER = LogManager.getLogger(RetryingRangeContentResponseHandler.class);
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
            LOGGER.warn("Premature EOF while downloading {} ({} / {})",
                    request, read, length);
            EntityUtils.consume(entity);
            LOGGER.warn("Enabling retrying range download");
            while (read < length) {
                LOGGER.debug("Downloading range {}: {} / {}",
                        request, read, length);
                RangeResponseHandler handler = new RangeResponseHandler(read, buffer);
                request.setHeader("Range", String.format(Locale.ROOT,
                        "bytes=%d-%d", read, length));
                Response response = executor.execute(request);
                response.handleResponse(handler);
                read = handler.totalRead;
            }
            LOGGER.debug("Done range downloading {}: {} / {}", request, read, length);
        }
        return new Content(buffer.toByteArray(), ContentType.getOrDefault(entity));
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

    private class RangeResponseHandler extends AbstractResponseHandler<Void> {
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
        public Void handleResponse(final HttpResponse response) throws IOException {
            final StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                throw new HttpResponseException(200, "Expected partial download response");
            } else if (statusCode != 206) {
                throw new HttpResponseException(statusCode, statusLine.getReasonPhrase());
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
