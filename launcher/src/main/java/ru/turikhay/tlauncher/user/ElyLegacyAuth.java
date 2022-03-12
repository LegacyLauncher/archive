package ru.turikhay.tlauncher.user;

import net.minecraft.launcher.Http;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.minecraft.auth.UUIDTypeAdapter;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;

public final class ElyLegacyAuth implements StandardAuth<ElyLegacyUser> {
    private static final String ELY_LEGACY_AUTHSERVER = "https://authserver.ely.by/auth/";
    // "https://authserver.ely.by/auth/authenticate", "https://authserver.ely.by/auth/refresh"
/*    protected AuthenticatorException getException(StandardAuthenticator.Response result) {
        if ("ForbiddenOperationException".equals(result.getError()) && "This account has been suspended.".equals(result.getErrorMessage()))
            return new UserBannedException();

        AuthenticatorException exception = super.getException(result);

        if (!exception.getClass().equals(AuthenticatorException.class)) // Known error
            return exception;

        if ("ServiceTemporarilyUnavailableException".equals(result.getError()))
            return new ServiceUnavailableException(result.getErrorMessage());

        return exception; // Error is still unknown
    }*/

    @Override
    public ElyLegacyUser authorize(String username, String password) throws AuthException, IOException {
        AuthenticationResponse response = new AuthenticationRequest(username, password, UUID.randomUUID().toString()).makeRequest();
        return new ElyLegacyUser(username, UUIDTypeAdapter.fromString(response.selectedProfile.id), response.selectedProfile.name, response.clientToken, response.accessToken);
    }

    @Override
    public void validate(ElyLegacyUser user) throws AuthException, IOException {
        RefreshResponse response = new RefreshRequest(user.getClientToken(), user.getAccessToken()).makeRequest();
        user.setToken(response.clientToken, response.accessToken);
        user.setDisplayName(response.selectedProfile.name);
    }

    private interface Validable {
        void validate() throws Exception;
    }

    private static abstract class Response implements Validable {
        String error;
        String errorMessage;
        String cause;

        @Override
        public void validate() throws Exception {
            if ("ForbiddenOperationException".equals(error)) {
                if ("Account protected with two factor auth.".equals(errorMessage)) {
                    throw new AuthException(errorMessage, "2fa");
                }
                if ("This account has been suspended.".equals(errorMessage)) {
                    throw new AuthBannedException();
                }
                throw new InvalidCredentialsException(errorMessage);
            }
            if ("ServiceTemporarilyUnavailableException".equals(error)) {
                throw new AuthUnavailableException(errorMessage);
            }
            if (StringUtils.isNotBlank(error) || StringUtils.isNotBlank(errorMessage)) {
                throw new AuthDetailedException(errorMessage);
            }
        }
    }

    private static abstract class Request<T extends Response> {
        abstract String url();

        abstract Class<T> responseClass();

        T makeRequest() throws AuthException, IOException {
            URL url = new URL(Objects.requireNonNull(url(), "url"));
            String input = U.getGson().toJson(this);
            String jsonResult = Http.performPostRequest(url, input, "application/json");
            T result;
            try {
                result = U.getGson().fromJson(jsonResult, responseClass());
                Objects.requireNonNull(result).validate();
            } catch (AuthException | IOException auth) {
                throw auth;
            } catch (Exception var7) {
                throw new AuthUnknownException(var7);
            }
            return result;
        }
    }

    private static class Agent {
        final String name = "Minecraft";
        final int version = 1;
    }

    private static class GameProfile implements Validable {
        String id, name;

        @Override
        public void validate() {
            StringUtil.requireNotBlank(id, "id");
            StringUtil.requireNotBlank(name, "name");
        }
    }

    private static class AuthenticationRequest extends Request<AuthenticationResponse> {
        final Agent agent = new Agent();
        final String username;
        final String password;
        final String clientToken;

        AuthenticationRequest(String username, String password, String clientToken) {
            this.username = StringUtil.requireNotBlank(username, "username");
            this.password = StringUtil.requireNotBlank(password, "password");
            this.clientToken = StringUtil.requireNotBlank(clientToken, "clientToken");
        }

        @Override
        String url() {
            return ELY_LEGACY_AUTHSERVER + "authenticate";
        }

        @Override
        Class<AuthenticationResponse> responseClass() {
            return AuthenticationResponse.class;
        }
    }

    private static class AuthenticationResponse extends Response {
        String accessToken;
        String clientToken;
        GameProfile selectedProfile;

        @Override
        public void validate() throws Exception {
            super.validate();
            StringUtil.requireNotBlank(accessToken, "accessToken");
            StringUtil.requireNotBlank(clientToken, "clientToken");
            Objects.requireNonNull(selectedProfile, "selectedProfile").validate();
        }
    }

    private static class RefreshRequest extends Request<RefreshResponse> {
        final String clientToken;
        final String accessToken;
        final boolean requestUser = true;

        RefreshRequest(String clientToken, String accessToken) {
            this.clientToken = StringUtil.requireNotBlank(clientToken, "clientToken");
            this.accessToken = StringUtil.requireNotBlank(accessToken, "accessToken");
        }

        @Override
        String url() {
            return ELY_LEGACY_AUTHSERVER + "refresh";
        }

        @Override
        Class<RefreshResponse> responseClass() {
            return RefreshResponse.class;
        }
    }

    protected static class RefreshResponse extends Response {
        String accessToken;
        String clientToken;
        GameProfile selectedProfile;

        @Override
        public void validate() throws Exception {
            super.validate();
            StringUtil.requireNotBlank(accessToken, "accessToken");
            StringUtil.requireNotBlank(clientToken, "clientToken");
            Objects.requireNonNull(selectedProfile, "selectedProfile").validate();
        }
    }
}
