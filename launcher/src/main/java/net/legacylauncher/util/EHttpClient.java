package net.legacylauncher.util;

import net.legacylauncher.ipc.ResolverIPC;
import net.legacylauncher.ipc.SystemDefaultResolverIPC;
import net.legacylauncher.util.http.HttpRequestRetryStrategy;
import net.legacylauncher.util.http.RetryingRangeContentResponseHandler;
import net.legacylauncher.util.ua.LauncherUserAgent;
import org.apache.hc.client5.http.fluent.Content;
import org.apache.hc.client5.http.fluent.Executor;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.TimeValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class EHttpClient {
    private static final Lazy<Registry<ConnectionSocketFactory>> defaultRegistry = Lazy.of(() -> RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", SSLConnectionSocketFactory.getSocketFactory())
            .build());
    private static ResolverIPC resolver = SystemDefaultResolverIPC.INSTANCE;
    private static final Lazy<CloseableHttpClient> globalClient = Lazy.of(() -> builder().build());

    private EHttpClient() {
    }

    public static ResolverIPC getGlobalResolver() {
        return resolver;
    }

    public static void setGlobalResolver(ResolverIPC resolver) {
        EHttpClient.resolver = resolver;
    }

    public static CloseableHttpClient getGlobalClient() {
        return globalClient.get();
    }

    public static HttpClientBuilder builder() {
        return HttpClients.custom()
                .setUserAgent(LauncherUserAgent.USER_AGENT)
                .setRetryStrategy(new HttpRequestRetryStrategy())
                .setConnectionManager(new PoolingHttpClientConnectionManager(
                        defaultRegistry.get(),
                        PoolConcurrencyPolicy.STRICT,
                        PoolReusePolicy.LIFO,
                        TimeValue.ofMinutes(1),
                        null,
                        resolver,
                        null));
    }

    public static CloseableHttpClient createRepeatable() {
        return builder().build();
    }

    public static Content toContent(CloseableHttpClient httpClient, Request request) throws IOException {
        Executor executor = Executor.newInstance(httpClient);
        return executor.execute(request).handleResponse(
                new RetryingRangeContentResponseHandler(request, executor)
        );
    }

    public static Content toContent(Request request) throws IOException {
        return toContent(getGlobalClient(), request);
    }

    public static String toString(CloseableHttpClient httpClient, Request request) throws IOException {
        Content content = toContent(httpClient, request);
        return content == null ? null : content.asString();
    }

    public static String toString(Request request) throws IOException {
        return toString(getGlobalClient(), request);
    }

    public static Reader toReader(HttpEntity entity) throws IOException {
        InputStream input = Objects.requireNonNull(entity.getContent(), "content");
        ContentType contentType = ContentType.parseLenient(entity.getContentType());
        Charset charset;
        if (contentType == null || contentType.getCharset() == null) {
            charset = StandardCharsets.UTF_8;
        } else {
            charset = contentType.getCharset();
        }
        return new InputStreamReader(input, charset);
    }
}
