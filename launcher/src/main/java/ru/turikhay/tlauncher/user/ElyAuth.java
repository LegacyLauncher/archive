package ru.turikhay.tlauncher.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;

public final class ElyAuth implements Auth<ElyUser> {
    private static final Logger LOGGER = LogManager.getLogger(ElyAuth.class);

    static final String CLIENT_ID = "tlauncher";
    static final String CLIENT_SECRET = "SbOVmJHBCjMV1NsewphGgA2SbyrVjN7IBcOte6b1HR7JGup2";

    static final String ACCOUNT_BASE = "https://account.ely.by";
    static final String API_BASE = ACCOUNT_BASE + "/api";
    static final String TOKEN_EXCHANGE = API_BASE + "/oauth2/v1/token";

    public PrimaryElyAuthFlow getPrimaryFlow() {
        return new PrimaryElyAuthFlow();
    }

    public FallbackElyAuthFlow getFallbackFlow() {
        return new FallbackElyAuthFlow();
    }

    // flow may be null
    ElyUser createUser(ElyAuthFlow<?> flow) throws Exception {
        return fetchCodeImpl(flow).getUser();
    }

    private ElyAuthCode fetchCodeImpl(ElyAuthFlow<?> flow) throws Exception {
        Objects.requireNonNull(flow);

        LOGGER.debug("Trying to get code using {}", flow);
        try {
            return flow.call();
        } catch (InterruptedException interrupted) {
            throw interrupted;
        } catch (Exception e) {
            LOGGER.error("Couldn't fetch code", e);
            throw e;
        }
    }

    @Override
    public void validate(ElyUser user) throws AuthException {
        Objects.requireNonNull(user, "user");
        ElyUserValidator validator = new ElyUserValidator(user);
        try {
            validator.validateUser();
        } catch (IOException e) {
            LOGGER.warn("Ely returned error", e);
            throw AuthException.soft(new AuthUnavailableException(e));
        }
    }
}
