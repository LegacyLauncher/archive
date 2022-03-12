package ru.turikhay.util;

import org.apache.http.HttpEntity;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
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

    public static HttpClientBuilder builder() {
        return HttpClients.custom()
                .setRetryHandler(new HttpRequestRetryHandler())
                .setServiceUnavailableRetryStrategy(new DefaultServiceUnavailableRetryStrategy());
    }

    public static CloseableHttpClient createRepeatable() {
        return builder().build();
    }

    public static Content toContent(CloseableHttpClient httpClient, Request request) throws IOException {
        try {
            Executor executor = Executor.newInstance(httpClient);
            return executor.execute(request).handleResponse(
                    new RetryingRangeContentResponseHandler(request, executor)
            );
        } finally {
            httpClient.close();
        }
    }

    public static Content toContent(Request request) throws IOException {
        return toContent(createRepeatable(), request);
    }

    public static String toString(CloseableHttpClient httpClient, Request request) throws IOException {
        return toContent(httpClient, request).asString();
    }

    public static String toString(Request request) throws IOException {
        return toString(createRepeatable(), request);
    }

    public static Reader toReader(HttpEntity entity) throws IOException {
        InputStream input = Objects.requireNonNull(entity.getContent(), "content");
        ContentType contentType = ContentType.get(entity);
        Charset charset;
        if (contentType == null || contentType.getCharset() == null) {
            charset = StandardCharsets.UTF_8;
        } else {
            charset = contentType.getCharset();
        }
        return new InputStreamReader(input, charset);
    }

    private EHttpClient() {
    }
}
