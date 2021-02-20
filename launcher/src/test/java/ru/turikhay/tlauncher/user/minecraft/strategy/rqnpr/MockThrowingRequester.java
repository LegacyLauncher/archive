package ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr;

import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class MockThrowingRequester {
    public static class InvalidResponse<A> implements Requester<A> {
        private final InvalidResponseException e;

        public InvalidResponse(InvalidResponseException e) {
            this.e = Objects.requireNonNull(e);
        }

        @Override
        public String makeRequest(Logger logger, A argument) throws InvalidResponseException {
            throw e;
        }
    }
}
