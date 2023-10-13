package net.legacylauncher.user.minecraft.strategy.xb;

import net.legacylauncher.user.minecraft.strategy.rqnpr.GsonParser;
import net.legacylauncher.user.minecraft.strategy.rqnpr.Parser;
import net.legacylauncher.user.minecraft.strategy.rqnpr.RequestAndParseStrategy;
import net.legacylauncher.user.minecraft.strategy.rqnpr.Requester;
import org.apache.logging.log4j.Logger;

public abstract class XboxServiceAuthStrategy
        extends RequestAndParseStrategy<String, XboxServiceAuthenticationResponse> {

    protected XboxServiceAuthStrategy(Logger logger,
                                      Requester<String> requester) {
        this(logger, requester, createGsonParser());
    }

    protected XboxServiceAuthStrategy(Logger logger,
                                      Requester<String> requester,
                                      Parser<XboxServiceAuthenticationResponse> parser) {
        super(logger, requester, parser);
    }

    protected static GsonParser<XboxServiceAuthenticationResponse> createGsonParser() {
        return GsonParser.withDeserializer(
                XboxServiceAuthenticationResponse.class,
                new XboxServiceAuthenticationResponse.Deserializer()
        );
    }
}
