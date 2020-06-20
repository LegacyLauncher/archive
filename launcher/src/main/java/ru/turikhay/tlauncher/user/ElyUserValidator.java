package ru.turikhay.tlauncher.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.git.MapTokenResolver;
import ru.turikhay.util.git.TokenReplacingReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

class ElyUserValidator {
    static final String ACCOUNT_INFO = ElyAuth.API_BASE + "/account/v1/info";
    static final String TOKEN_REFRESH_REQUEST = "grant_type=refresh_token&client_id="+ ElyAuth.CLIENT_ID +"&" +
            "client_secret="+ ElyAuth.CLIENT_SECRET +"&refresh_token=${refresh_token}";

    private String accessToken;
    private String refreshToken;
    private Long expiryTime;

    private ElyUser user;

    private final Gson gson = new GsonBuilder().create();

    ElyUserValidator(ElyUser user) {
        U.requireNotNull(user, "user");
        updateUser(user);

        log("user validator created for user:", user);
    }

    public void validateUser() throws IOException, InvalidCredentialsException {
        log("validating user...");

        Response<ElyUserJsonizer.ElySerialize> rawUserResponse = getUserInfo();
        renewToken: {
            if (rawUserResponse.error == null) {
                break renewToken;
            }

            renewTokenBreak: {
                if(rawUserResponse.error.isTokenExpired()) {
                    log("token has expired. renewing");
                    break renewTokenBreak;
                }

                if (expiryTime != null && expiryTime - System.currentTimeMillis() < 1000 * 60 * 60 * 2 /* 2 hours */) {
                    if (refreshToken == null) {
                        break renewToken; // we cannot refresh token, so run the game with token we has
                    }

                    log("token will expire in less than two hours. renewing");
                    break renewTokenBreak;
                }

                rawUserResponse = getUserInfo();
                if(rawUserResponse.error != null) {
                    if(rawUserResponse.error.isTokenExpired() && refreshToken != null) {
                        log("token has expired. renewing");
                        break renewTokenBreak;
                    } else {
                        throw new InvalidCredentialsException(rawUserResponse.error.toString());
                    }
                }

                break renewToken; // do not renewToken
            }

            TokenRefreshResponse response = refreshAccessToken();
            setToken(response.access_token, refreshToken, System.currentTimeMillis() + (response.expires_in * 1000));

            rawUserResponse = getUserInfo();
            if(rawUserResponse.error != null) {
                throw new InvalidCredentialsException(rawUserResponse.error.toString());
            }
        }

        ElyUserJsonizer.ElySerialize rawUser = rawUserResponse.response;
        rawUser.accessToken = accessToken;
        rawUser.refreshToken = refreshToken;
        rawUser.expiryTime = expiryTime;
        updateUser(rawUser.create());
    }

    private TokenRefreshResponse refreshAccessToken() throws IOException, InvalidCredentialsException {
        log("refreshing access token");

        String request = TokenReplacingReader.resolveVars(TOKEN_REFRESH_REQUEST, new MapTokenResolver(new HashMap<String, String>() {
            {
                put("refresh_token", refreshToken);
            }
        }));

        HttpURLConnection connection = setupConnection(ElyAuth.TOKEN_EXCHANGE);
        connection.setDoOutput(true);
        IOUtils.write(request, connection.getOutputStream(), FileUtil.getCharset());

        Response<TokenRefreshResponse> tokenRefreshResponse = parse(connection, TokenRefreshResponse.class);

        if(tokenRefreshResponse.error != null) {
            throw new InvalidCredentialsException(tokenRefreshResponse.error.toString());
        }

        TokenRefreshResponse response = tokenRefreshResponse.response;

        try {
            response.checkConsistancy();
        } catch(RuntimeException rE) {
            throw new IOException("token response is invalid");
        }

        return response;
    }

    private void updateUser(ElyUser user) {
        U.requireNotNull(user, "user");
        if(this.user == null) {
            this.user = user;
        } else {
            this.user.copyFrom(user);
        }
        setToken(user.getAccessToken(), user.getRefreshToken(), user.getExpiryTime());
    }

    private void setToken(String accessToken, String refreshToken, Long expiryTime) {
        this.accessToken = StringUtil.requireNotBlank(accessToken, "accessToken");
        this.refreshToken = refreshToken;

        if (expiryTime != null && expiryTime < 0) {
            throw new IllegalArgumentException("expiryTime");
        }

        this.expiryTime = expiryTime;

        if(user != null) {
            user.setToken(accessToken, refreshToken, expiryTime);
        }
    }

    private static HttpURLConnection setupConnection(String _url) throws IOException {
        URL url = new URL(_url);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setReadTimeout(U.getReadTimeout());
        connection.setConnectTimeout(U.getConnectionTimeout());

        return connection;
    }

    private <T> T parse(InputStream input, Class<T> clazz) throws IOException {
        String response = IOUtils.toString(input, FileUtil.getCharset());
        log("Response:", response);
        try {
            return gson.fromJson(response, clazz);
        } catch(RuntimeException rE) {
            throw new IOException("could not parse response: \""+ response +"\"", rE);
        }
    }

    private <T> Response<T> parse(HttpURLConnection connection, Class<T> clazz) throws IOException {
        if(connection.getResponseCode() == 200) {
            try {
                return new Response<T>(parse(connection.getInputStream(), clazz));
            } catch (Exception e) {
                throw new IOException("could not parse response", e);
            }
        }

        YiiErrorResponse error;
        try {
            error = parse(connection.getErrorStream(), YiiErrorResponse.class);
        } catch(Exception e) {
            throw new IOException("could not read error message", e);
        }

        return new Response<T>(error);
    }

    private Response<ElyUserJsonizer.ElySerialize> getUserInfo() throws IOException {
        HttpURLConnection connection = setupConnection(ACCOUNT_INFO);
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        return parse(connection, ElyUserJsonizer.ElySerialize.class);
    }

    static class TokenExpiredException extends InvalidCredentialsException {
        TokenExpiredException(String message) {
            super(message);
        }
    }

    static class YiiErrorResponse {
        private String name, message, type;
        private int code, status;

        boolean isTokenExpired() {
            return "Token expired".equals(message) || "Unauthorized".equals(name);
        }

        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("name", name)
                    .append("message", message)
                    .append("type", type)
                    .append("code", code)
                    .append("status")
                    .build();
        }
    }

    static class TokenRefreshResponse {
        private String access_token, token_type;
        private int expires_in;

        void checkConsistancy() {
            StringUtil.requireNotBlank(access_token, "access_token");
            if(!"Bearer".equals(token_type)) {
                throw new IllegalArgumentException("token type: \""+ token_type +"\"");
            }
            if(expires_in != 0 && expires_in < 0) {
                throw new IllegalArgumentException("expires_in: " + expires_in);
            }
        }
    }

    static class Response<T> {
        final YiiErrorResponse error;
        final T response;

        Response(YiiErrorResponse error, T response) {
            this.error = error;
            this.response = response;

            if(error == null && response == null) {
                throw new NullPointerException("error & response");
            }
        }

        Response(YiiErrorResponse error) {
            this(U.requireNotNull(error, "error"), null);
        }

        Response(T response) {
            this(null, U.requireNotNull(response, "response"));
        }
    }

    private final String logPrefix = '[' + getClass().getSimpleName() + ']';
    private void log(Object... o) {
        U.log(logPrefix, o);
    }
}