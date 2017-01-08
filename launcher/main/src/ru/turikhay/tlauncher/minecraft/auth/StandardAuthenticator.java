package ru.turikhay.tlauncher.minecraft.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.launcher.Http;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.util.U;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.UUID;

public class StandardAuthenticator extends Authenticator {
    protected final Gson gson = (new GsonBuilder()).registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
    protected final URL AUTHENTICATE_URL;
    protected final URL REFRESH_URL;

    protected StandardAuthenticator(Account account, String authUrl, String refreshUrl) {
        super(account);
        AUTHENTICATE_URL = Http.constantURL(authUrl);
        REFRESH_URL = Http.constantURL(refreshUrl);
    }

    protected void pass() throws AuthenticatorException {
        if (account.isFree()) {
            throw new IllegalArgumentException("invalid account type");
        } else if (account.getPassword() == null && account.getAccessToken() == null) {
            throw new AuthenticatorException(new NullPointerException("password/accessToken"));
        } else {
            log(account);
            log("hasUsername:", account.getUsername());
            log("hasPassword:", Boolean.valueOf(account.getPassword() != null));
            log("hasAccessToken:", Boolean.valueOf(account.getAccessToken() != null));

            if (account.getPassword() == null) {
                tokenLogin();
            } else {
                passwordLogin();
            }

            log("Log in successful!");
            log("hasUUID:", Boolean.valueOf(account.getUUID() != null));
            log("hasAccessToken:", Boolean.valueOf(account.getAccessToken() != null));
            log("hasProfiles:", Boolean.valueOf(account.getProfiles() != null));
            log("hasProfile:", Boolean.valueOf(account.getProfiles() != null));
            log("hasProperties:", Boolean.valueOf(account.getProperties() != null));
            log(account.getProfile());
        }
    }

    protected void passwordLogin() throws AuthenticatorException {
        log("Loggining in with password");
        StandardAuthenticator.AuthenticationRequest request = new StandardAuthenticator.AuthenticationRequest(this);
        StandardAuthenticator.AuthenticationResponse response = makeRequest(AUTHENTICATE_URL, request, AuthenticationResponse.class);

        account.setUserID(response.getUserID() != null ? response.getUserID() : account.getUsername());
        account.setAccessToken(response.getAccessToken());
        account.setPassword((String) null);
        account.setProfiles(response.getAvailableProfiles());
        account.setProfile(response.getSelectedProfile());
        account.setUser(response.getUser());
        setClientToken(response.getClientToken());

        if (response.getSelectedProfile() != null) {
            account.setUUID(response.getSelectedProfile().getId());
            account.setDisplayName(response.getSelectedProfile().getName());
        }

    }

    protected void tokenLogin() throws AuthenticatorException {
        log("Loggining in with token");
        StandardAuthenticator.RefreshRequest request = new StandardAuthenticator.RefreshRequest(this);
        StandardAuthenticator.RefreshResponse response = makeRequest(REFRESH_URL, request, RefreshResponse.class);

        account.setAccessToken(response.getAccessToken());
        account.setProfile(response.getSelectedProfile());
        account.setUser(response.getUser());
        setClientToken(response.getClientToken());

        if (response.getSelectedProfile() != null) {
            account.setUUID(response.getSelectedProfile().getId());
            account.setDisplayName(response.getSelectedProfile().getName());
        }
    }

    protected <T extends StandardAuthenticator.Response> T makeRequest(URL url, StandardAuthenticator.Request input, Class<T> classOfT) throws AuthenticatorException {
        if (url == null) {
            throw new NullPointerException("url");
        } else {
            String jsonResult;
            try {
                if (input == null) {
                    jsonResult = AuthenticatorService.performGetRequest(url);
                } else {
                    jsonResult = AuthenticatorService.performPostRequest(url, gson.toJson(input), "application/json");
                }
            } catch (IOException var8) {
                throw new AuthenticatorException("Error making request, uncaught IOException", "unreachable", var8);
            }

            T result;
            try {
                result = gson.fromJson(jsonResult, classOfT);
            } catch (RuntimeException var7) {
                throw new AuthenticatorException("Error parsing response: \"" + jsonResult + "\"", "unparseable", var7);
            }

            if (result == null) {
                return null;
            } else if (StringUtils.isBlank(result.getError())) {
                return result;
            } else {
                throw getException(result);
            }
        }
    }

    protected AuthenticatorException getException(StandardAuthenticator.Response result) {
        return "UserMigratedException".equals(result.getCause()) ? new UserMigratedException() : ("ForbiddenOperationException".equals(result.getError()) ? new InvalidCredentialsException() : new AuthenticatorException(result, "internal"));
    }

    protected static class AuthenticationRequest extends StandardAuthenticator.Request {
        private Agent agent;
        private String username;
        private String password;
        private String clientToken;

        protected AuthenticationRequest(String username, String password, String clientToken) {
            agent = Agent.MINECRAFT;
            this.username = username;
            this.password = password;
            this.clientToken = clientToken;
        }

        protected AuthenticationRequest(Authenticator auth) {
            this(auth.account.getUsername(), auth.account.getPassword(), Authenticator.getClientToken().toString());
        }

        public Agent getAgent() {
            return agent;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getClientToken() {
            return clientToken;
        }

        public boolean isRequestingUser() {
            return true;
        }
    }

    protected static class AuthenticationResponse extends StandardAuthenticator.Response {
        private String accessToken;
        private String clientToken;
        private GameProfile selectedProfile;
        private GameProfile[] availableProfiles;
        private User user;

        public String getAccessToken() {
            return accessToken;
        }

        public String getClientToken() {
            return clientToken;
        }

        public GameProfile[] getAvailableProfiles() {
            return availableProfiles;
        }

        public GameProfile getSelectedProfile() {
            return selectedProfile;
        }

        public User getUser() {
            return user;
        }

        public String getUserID() {
            return user != null ? user.getID() : null;
        }
    }

    protected static class RefreshRequest extends StandardAuthenticator.Request {
        private String clientToken;
        private String accessToken;
        private GameProfile selectedProfile;
        private boolean requestUser;

        private RefreshRequest(String clientToken, String accessToken, GameProfile profile) {
            requestUser = true;
            this.clientToken = clientToken;
            this.accessToken = accessToken;
            selectedProfile = profile;
        }

        RefreshRequest(String clientToken, String accessToken) {
            this(clientToken, accessToken, null);
        }

        private RefreshRequest(Authenticator auth, GameProfile profile) {
            this(Authenticator.getClientToken().toString(), auth.account.getAccessToken(), profile);
        }

        RefreshRequest(Authenticator auth) {
            this(auth, null);
        }

        public String getClientToken() {
            return clientToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public GameProfile getProfile() {
            return selectedProfile;
        }
    }

    protected static class RefreshResponse extends StandardAuthenticator.Response {
        private String accessToken;
        private String clientToken;
        private GameProfile selectedProfile;
        private User user;

        public String getAccessToken() {
            return accessToken;
        }

        public String getClientToken() {
            return clientToken;
        }

        public GameProfile getSelectedProfile() {
            return selectedProfile;
        }

        public User getUser() {
            return user;
        }
    }

    protected static class Request {
    }

    protected static class Response {
        private String error;
        private String errorMessage;
        private String cause;

        public String getError() {
            return error;
        }

        public String getCause() {
            return cause;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
