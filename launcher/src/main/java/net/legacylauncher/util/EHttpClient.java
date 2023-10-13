package net.legacylauncher.util;

import net.legacylauncher.ipc.ResolverIPC;
import net.legacylauncher.ipc.SystemDefaultResolverIPC;
import net.legacylauncher.util.http.HttpRequestRetryHandler;
import net.legacylauncher.util.http.RetryingRangeContentResponseHandler;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class EHttpClient {
    private static ResolverIPC resolver = SystemDefaultResolverIPC.INSTANCE;

    public static void setGlobalResolver(ResolverIPC resolver) {
        EHttpClient.resolver = resolver;
    }

    private static final Lazy<Registry<ConnectionSocketFactory>> defaultRegistry = Lazy.of(() -> RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", SSLConnectionSocketFactory.getSocketFactory())
            .build());
    private static final Lazy<HttpClient> globalClient = Lazy.of(() -> builder().build());

    public static HttpClient getGlobalClient() {
        return globalClient.get();
    }

    public static HttpClientBuilder builder() {
        return HttpClients.custom()
                .setDnsResolver(resolver)
                .setRetryHandler(new HttpRequestRetryHandler())
                .setConnectionManager(new PoolingHttpClientConnectionManager(defaultRegistry.get(), resolver))
                .setServiceUnavailableRetryStrategy(new DefaultServiceUnavailableRetryStrategy());
    }

    public static CloseableHttpClient createRepeatable() {
        return builder().build();
    }

    public static Content toContent(HttpClient httpClient, Request request) throws IOException {
        Executor executor = Executor.newInstance(httpClient);
        return executor.execute(request).handleResponse(
                new RetryingRangeContentResponseHandler(request, executor)
        );
    }

    public static Content toContent(Request request) throws IOException {
        return toContent(getGlobalClient(), request);
    }

    public static String toString(HttpClient httpClient, Request request) throws IOException {
        Content content = toContent(httpClient, request);
        return content == null ? null : content.asString();
    }

    public static String toString(Request request) throws IOException {
        return toString(getGlobalClient(), request);
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
