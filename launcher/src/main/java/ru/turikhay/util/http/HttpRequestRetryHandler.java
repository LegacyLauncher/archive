package ru.turikhay.util.http;

import org.apache.http.HttpRequest;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

public class HttpRequestRetryHandler extends DefaultHttpRequestRetryHandler {
    private static final Collection<Class<? extends IOException>> NON_RETRIABLE = Collections.singletonList(
            UnknownHostException.class
    );

    private static final Collection<String> IDEMPOTENT_METHODS = Arrays.asList(
            "GET", "HEAD", "PUT", "DELETE", "OPTIONS", "TRACE" // as in StandardHttpRequestRetryHandler
    );

    public HttpRequestRetryHandler(int retryCount, boolean requestSentRetryEnabled) {
        super(retryCount, requestSentRetryEnabled, NON_RETRIABLE);
    }

    public HttpRequestRetryHandler() {
        this(3, false);
    }

    @Override
    protected boolean handleAsIdempotent(final HttpRequest request) {
        final String method = request.getRequestLine().getMethod().toUpperCase(Locale.ROOT);
        return IDEMPOTENT_METHODS.contains(method);
    }
}
