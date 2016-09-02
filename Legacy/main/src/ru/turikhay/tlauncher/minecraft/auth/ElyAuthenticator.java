package ru.turikhay.tlauncher.minecraft.auth;

import java.util.Map;

public class ElyAuthenticator extends StandardAuthenticator {
    private static final String ELY_URL = "https://authserver.ely.by/auth/";

    public ElyAuthenticator(Account account) {
        super(account, "https://authserver.ely.by/auth/authenticate", "https://authserver.ely.by/auth/refresh");
    }

    protected AuthenticatorException getException(StandardAuthenticator.Response result) {
        if ("ForbiddenOperationException".equals(result.getError()) && "This account has been suspended.".equals(result.getErrorMessage()))
            return new UserBannedException();

        AuthenticatorException exception = super.getException(result);

        if (!exception.getClass().equals(AuthenticatorException.class)) // Known error
            return exception;

        if ("ServiceTemporarilyUnavailableException".equals(result.getError()))
            return new ServiceUnavailableException(result.getErrorMessage());

        return exception; // Error is still unknown
    }
}
