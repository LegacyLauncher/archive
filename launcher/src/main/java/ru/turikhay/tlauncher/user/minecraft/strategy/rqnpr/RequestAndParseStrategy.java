package ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr;

import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.exceptions.ParseException;

import java.io.IOException;

public abstract class RequestAndParseStrategy<A, V extends Validatable> {
    private final Logger logger;
    private final Requester<A> requester;
    private final Parser<V> parser;

    protected RequestAndParseStrategy(Logger logger, Requester<A> requester, Parser<V> parser) {
        this.logger = logger;
        this.requester = requester;
        this.parser = parser;
    }

    protected V requestAndParse(A argument) throws IOException, InvalidResponseException {
        String response = requester.makeRequest(logger, argument);
        try {
            return parser.parseResponse(logger, response);
        } catch (ParseException e) {
            throw new InvalidResponseException(response, e);
        }
    }
}
