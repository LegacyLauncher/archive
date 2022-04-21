package ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.EHttpClient;

import java.io.IOException;
import java.util.function.Function;

public class HttpClientRequester<A> implements Requester<A> {
    private final Executor requestExecutor;
    private final Function<A, Request> requestFactory;

    public HttpClientRequester(Executor requestExecutor, Function<A, Request> requestFactory) {
        this.requestExecutor = requestExecutor;
        this.requestFactory = requestFactory;
    }

    public HttpClientRequester(Function<A, Request> requestFactory) {
        this(Executor.newInstance(EHttpClient.createRepeatable()), requestFactory);
    }

    @Override
    public String makeRequest(Logger logger, A argument) throws InvalidResponseException, IOException {
        Request request = this.requestFactory.apply(argument);
        logger.trace("Sending request: {}", request);
        HttpResponse httpResponse = this.requestExecutor.execute(request).returnResponse();
        logger.trace("Reading response");
        String response = EntityUtils.toString(httpResponse.getEntity());
        logger.trace("Response: {}", response);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        logger.trace("Status code: {}", statusCode);
        if (statusCode >= 200 && statusCode <= 299) {
            return response;
        }
        throw new InvalidStatusCodeException(statusCode, response);
    }
}
