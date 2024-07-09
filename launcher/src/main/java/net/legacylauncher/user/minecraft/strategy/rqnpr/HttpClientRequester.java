package net.legacylauncher.user.minecraft.strategy.rqnpr;

import lombok.SneakyThrows;
import net.legacylauncher.util.EHttpClient;
import org.apache.hc.client5.http.fluent.Executor;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;

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
        return requestExecutor.execute(request).handleResponse(httpResponse -> handleRequest(logger, httpResponse));
    }

    @SneakyThrows(InvalidResponseException.class)
    private String handleRequest(Logger logger, ClassicHttpResponse httpResponse) throws IOException, ParseException {
        logger.trace("Reading response");
        String response = EntityUtils.toString(httpResponse.getEntity());
        logger.trace("Response: {}", response);
        int statusCode = httpResponse.getCode();
        logger.trace("Status code: {}", statusCode);
        if (statusCode >= 200 && statusCode <= 299) {
            return response;
        }
        throw new InvalidStatusCodeException(statusCode, response);
    }
}
