package ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr;

import org.apache.logging.log4j.Logger;

import java.util.function.Function;

public class MockStringRequester<A> implements Requester<A> {
    private final Function<A, String> responseFactory;

    public MockStringRequester(Function<A, String> responseFactory) {
        this.responseFactory = responseFactory;
    }

    @Override
    public String makeRequest(Logger logger, A argument) {
        return responseFactory.apply(argument);
    }
}
