package net.legacylauncher.util.http;


import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.util.TimeValue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

public class HttpRequestRetryStrategy extends DefaultHttpRequestRetryStrategy {
    private static final Collection<Class<? extends IOException>> NON_RETRIABLE = Collections.singletonList(
            UnknownHostException.class
    );

    private static final Set<String> IDEMPOTENT_METHODS = new HashSet<>(Arrays.asList(
            "GET", "HEAD", "PUT", "DELETE", "OPTIONS", "TRACE" // as in StandardHttpRequestRetryHandler
    ));

    public HttpRequestRetryStrategy() {
        super(3, TimeValue.ofSeconds(1), NON_RETRIABLE, Collections.emptyList());
    }

    @Override
    protected boolean handleAsIdempotent(final HttpRequest request) {
        final String method = request.getMethod().toUpperCase(Locale.ROOT);
        return IDEMPOTENT_METHODS.contains(method);
    }
}
