package net.legacylauncher.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.util.StringUtil;
import net.legacylauncher.util.U;
import net.legacylauncher.util.git.MapTokenResolver;
import net.legacylauncher.util.git.TokenReplacingReader;
import net.legacylauncher.util.ua.LauncherUserAgent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

@Slf4j
public final class ElyAuthCode {
    static final String API_BASE = ElyAuth.ACCOUNT_BASE + "/api";
    static final String ACCOUNT_INFO = API_BASE + "/account/v1/info";
    static final String TOKEN_EXCHANGE = API_BASE + "/oauth2/v1/token";

    static final String TOKEN_EXCHANGE_REQUEST = "grant_type=authorization_code&client_id=${client_id}&" +
            "client_secret=${client_secret}&code=${code}&state=${state}&redirect_uri=${redirect_uri}";

    final String code, redirect_uri;
    final int state;

    private final Gson gson;

    ElyAuthCode(String code, String redirect_uri, int state) {
        this.code = StringUtil.requireNotBlank(code, "code");
        this.redirect_uri = StringUtil.requireNotBlank(redirect_uri, "redirect_uri");
        this.state = state;

        this.gson = new GsonBuilder()/*.registerTypeAdapter(ElyUser.class, new ElyUserJsonizer())*/.create();

        log.info("Created with: code {}, redirect_uri {}, state {}", code, redirect_uri, state);
    }

    public ElyUser getUser() throws IOException, AuthException {
        CodeExchangePayload codeExchangePayload = exchangeCode();
        return getRawUser(codeExchangePayload).create();
    }

    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("code", code)
                .append("redirect_uri", redirect_uri)
                .append("state", state)
                .build();
    }

    CodeExchangePayload exchangeCode() throws IOException, AuthException {
        log.debug("Exchanging code...");

        CodeExchangePayload payload = readResponse(setupExchangeConnection(), CodeExchangePayload.class);

        log.debug("Checking payload consistency...");
        payload.checkConsistency();

        log.debug("Done");
        return payload;
    }

    ElyUserJsonizer.ElySerialize getRawUser(CodeExchangePayload payload) throws IOException, AuthException {
        log.debug("Getting user using payload...");
        ElyUserJsonizer.ElySerialize serialize = readResponse(setupInfoConnection(payload), ElyUserJsonizer.ElySerialize.class);

        serialize.accessToken = payload.access_token;
        serialize.refreshToken = payload.refresh_token;
        if (payload.expires_in != 0) {
            serialize.expiryTime = U.getUTC().getTimeInMillis() + payload.expires_in * 1000L;
        } else {
            String[] jwtTokenParts = payload.access_token.split("\\.");
            if (jwtTokenParts.length != 3) {
                throw new IllegalArgumentException("Cannot determine token lifetime. The token is " + payload.access_token);
            }

            byte[] decoded = Base64.getUrlDecoder().decode(jwtTokenParts[1].getBytes(StandardCharsets.UTF_8));
            JWTPayload jwtPayload = parse(new ByteArrayInputStream(decoded), JWTPayload.class);
            if (jwtPayload.exp != 0.f) {
                serialize.expiryTime = (long) (jwtPayload.exp * 1000.f);
            }
        }

        log.debug("User: {}", gson.toJson(serialize));
        return serialize;
    }

    <T> T readResponse(HttpURLConnection connection, Class<T> clazz) throws IOException, AuthException {
        IOException ioE = null;
        byte[] read;

        try (InputStream input = connection.getInputStream()) {
            read = IOUtils.toByteArray(input);
        } catch (IOException e) {
            ioE = e;
            try (InputStream error = connection.getErrorStream()) {
                read = IOUtils.toByteArray(error);
            } catch (IOException suppressedIoE) {
                ioE.addSuppressed(suppressedIoE);
                throw ioE;
            }
        }

        if (ioE != null) {
            throw detailed(read);
        }

        try {
            return this.parse(new ByteArrayInputStream(read), clazz);
        } catch (RuntimeException rE) {
            AuthDetailedException detailedException = detailed(read);
            detailedException.addSuppressed(rE);
            throw detailedException;
        }
    }

    <T> T parse(InputStream in, Class<T> clazz) {
        return gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), clazz);
    }

    private AuthDetailedException detailed(byte[] data) {
        return new AuthDetailedException(new String(data, StandardCharsets.UTF_8));
    }

    HttpURLConnection setupExchangeConnection() throws IOException {
        log.debug("Setting up exchange connection...");

        String request = TokenReplacingReader.resolveVars(TOKEN_EXCHANGE_REQUEST, new MapTokenResolver(new HashMap<String, String>() {
            {
                put("client_id", ElyAuth.CLIENT_ID);
                put("client_secret", ElyAuth.CLIENT_SECRET);
                put("code", code);
                put("state", String.valueOf(state));
                put("redirect_uri", redirect_uri);
            }
        }));
        log.debug("Request: {}", request);

        HttpURLConnection connection = setupConnection("POST", TOKEN_EXCHANGE);

        connection.setDoOutput(true);
        log.debug("Writing request...");
        IOUtils.write(request, connection.getOutputStream(), StandardCharsets.UTF_8);
        log.debug("Done, reading response");

        return connection;
    }

    HttpURLConnection setupInfoConnection(CodeExchangePayload codePayload) throws IOException {
        HttpURLConnection connection = setupConnection("GET", ACCOUNT_INFO);
        connection.setRequestProperty("Authorization", "Bearer " + codePayload.access_token);
        return connection;
    }

    static HttpURLConnection setupConnection(String method, String url) throws IOException {
        URL _url = new URL(url);

        HttpURLConnection urlConnection = (HttpURLConnection) _url.openConnection();
        urlConnection.setRequestMethod(method);
        urlConnection.setUseCaches(false);
        urlConnection.setReadTimeout(U.getReadTimeout());
        urlConnection.setConnectTimeout(U.getConnectionTimeout());
        LauncherUserAgent.set(urlConnection);

        return urlConnection;
    }

    private static class CodeExchangePayload {
        String access_token, token_type, refresh_token;
        int expires_in;

        void checkConsistency() {
            StringUtil.requireNotBlank(access_token, "access_token");

            if (!"Bearer".equals(token_type)) {
                throw new IllegalArgumentException("token_type: " + token_type);
            }

            if (expires_in != 0 && expires_in < 0) {
                throw new IllegalArgumentException("expires_in: " + expires_in);
            }
        }
    }

    private static class JWTPayload {
        float iat, exp;
    }
}
