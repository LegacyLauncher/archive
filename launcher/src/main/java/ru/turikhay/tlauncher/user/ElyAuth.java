package ru.turikhay.tlauncher.user;

import ru.turikhay.tlauncher.sentry.Sentry;
import ru.turikhay.util.DataBuilder;
import ru.turikhay.util.U;

import java.io.IOException;

public final class ElyAuth implements Auth<ElyUser> {
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
    ElyUser createUser(ElyAuthFlow flow) throws Exception {
        return fetchCodeImpl(flow).getUser();
    }

    private ElyAuthCode fetchCodeImpl(ElyAuthFlow flow) throws Exception {
        U.requireNotNull(flow);

        log("Try to get code with", flow);
        try {
            return flow.call();
        } catch (InterruptedException interrupted) {
            throw interrupted;
        } catch(Exception e) {
            Sentry.sendError(ElyAuth.class, "could not get code with " + flow.getClass().getSimpleName(), e, null);
            throw e;
        }
    }

    @Override
    public void validate(ElyUser user) throws AuthException, IOException {
        U.requireNotNull(user, "user");
        ElyUserValidator validator = new ElyUserValidator(user);
        try {
            validator.validateUser();
        } catch(IOException e) {
            Sentry.sendError(ElyAuth.class, "Ely soft exception", e, DataBuilder.create("user", user));
            throw AuthException.soft(new AuthUnavailableException(e));
        }
    }

    private final String logPrefix = "[" + getClass().getSimpleName() + "]";
    private void log(Object...o) {
        U.log(logPrefix, o);
    }
}
