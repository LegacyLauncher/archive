package net.legacylauncher.user.minecraft.strategy.rqnpr;

import org.slf4j.Logger;

import java.io.IOException;

@FunctionalInterface
public interface Requester<A> {
    String makeRequest(Logger logger, A argument) throws InvalidResponseException, IOException;
}
