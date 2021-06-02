package ru.turikhay.util;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;

public final class EHttpClient {

    public static CloseableHttpClient createRepeatable() {
        return HttpClients.custom()
                .setRetryHandler(new StandardHttpRequestRetryHandler())
                .setServiceUnavailableRetryStrategy(new DefaultServiceUnavailableRetryStrategy())
                .build();
    }

    private EHttpClient() {
    }
}
