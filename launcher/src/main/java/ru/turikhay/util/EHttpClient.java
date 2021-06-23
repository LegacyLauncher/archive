package ru.turikhay.util;

import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;

import java.io.IOException;

public final class EHttpClient {

    public static CloseableHttpClient createRepeatable() {
        return HttpClients.custom()
                .setRetryHandler(new StandardHttpRequestRetryHandler())
                .setServiceUnavailableRetryStrategy(new DefaultServiceUnavailableRetryStrategy())
                .build();
    }

    public static Response execute(Request request) throws IOException {
        try(CloseableHttpClient httpClient = createRepeatable()) {
            return Executor.newInstance(httpClient).execute(request);
        }
    }

    private EHttpClient() {
    }
}
