package ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr;

import org.apache.logging.log4j.Logger;

import java.io.IOException;

public interface Requester<A> {
    String makeRequest(Logger logger, A argument) throws InvalidResponseException, IOException;
}
