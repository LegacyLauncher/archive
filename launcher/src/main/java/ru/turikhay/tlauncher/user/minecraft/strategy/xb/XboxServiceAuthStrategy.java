package ru.turikhay.tlauncher.user.minecraft.strategy.xb;

import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.GsonParser;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.Parser;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.RequestAndParseStrategy;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.Requester;

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
