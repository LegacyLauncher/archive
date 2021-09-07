package ru.turikhay.util;

import org.apache.http.HttpEntity;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.HttpClients;
import ru.turikhay.util.http.HttpRequestRetryHandler;
import ru.turikhay.util.http.RetryingRangeContentResponseHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class EHttpClient {

    public static CloseableHttpClient createRepeatable() {
        return HttpClients.custom()
                .setRetryHandler(new HttpRequestRetryHandler())
                .setServiceUnavailableRetryStrategy(new DefaultServiceUnavailableRetryStrategy())
                .build();
    }

    public static Content toContent(Request request) throws IOException {
        try(CloseableHttpClient httpClient = createRepeatable()) {
            Executor executor = Executor.newInstance(httpClient);
            return executor.execute(request).handleResponse(
                    new RetryingRangeContentResponseHandler(request, executor)
            );
        }
    }

    public static String toString(Request request) throws IOException {
        return toContent(request).asString();
    }

    public static Reader toReader(HttpEntity entity) throws IOException {
        InputStream input = Objects.requireNonNull(entity.getContent(), "content");
        ContentType contentType = ContentType.get(entity);
        Charset charset;
        if(contentType == null || contentType.getCharset() == null) {
            charset = StandardCharsets.UTF_8;
        } else {
            charset = contentType.getCharset();
        }
        return new InputStreamReader(input, charset);
    }

    private EHttpClient() {
    }
}
