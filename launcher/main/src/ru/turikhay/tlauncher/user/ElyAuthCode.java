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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

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

        log("Created with:", code, redirect_uri, state);
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
        log("Exchanging code...");

        CodeExchangePayload payload = parse(setupExchangeConnection(), CodeExchangePayload.class);

        log("Checking payload consistency...");
        payload.checkConsistency();

        log("Done");
        return payload;
    }

    ElyUserJsonizer.ElySerialize getRawUser(CodeExchangePayload payload) throws IOException, AuthException {
        log("Getting user using payload...");
        ElyUserJsonizer.ElySerialize serialize = parse(setupInfoConnection(payload), ElyUserJsonizer.ElySerialize.class);

        serialize.accessToken = payload.access_token;
        serialize.refreshToken = payload.refresh_token;
        serialize.expiryTime = U.getUTC().getTimeInMillis() + payload.expires_in;

        log("User:", gson.toJson(serialize));
        return serialize;
    }

    <T> T parse(HttpURLConnection connection, Class<T> clazz) throws IOException, AuthException {
        IOException ioE = null;
        byte[] read;

        try(InputStream input = connection.getInputStream()){
            read = IOUtils.toByteArray(input);
        } catch(IOException e) {
            U.close(connection.getInputStream());
            ioE = e;
            try(InputStream error = connection.getErrorStream()){
                read = IOUtils.toByteArray(error);
            } catch(IOException suppressedIoE) {
                ioE.addSuppressed(suppressedIoE);
                throw ioE;
            }
        }

        if(ioE != null) {
            throw detailed(read);
        }

        T result;
        try {
            result = gson.fromJson(new InputStreamReader(new ByteArrayInputStream(read), FileUtil.getCharset()), clazz);
        } catch(RuntimeException rE) {
            AuthDetailedException detailedException = detailed(read);
            detailedException.addSuppressed(rE);
            throw detailedException;
        }
        return result;
    }

    private AuthDetailedException detailed(byte[] data) {
        return new AuthDetailedException(new String(data, FileUtil.getCharset()));
    }

    HttpURLConnection setupExchangeConnection() throws IOException {
        log("Setting up exchange connection...");

        String request = TokenReplacingReader.resolveVars(TOKEN_EXCHANGE_REQUEST, new MapTokenResolver(new HashMap<String, String>(){
            {
                put("client_id", "tlauncher");
                put("client_secret", "SbOVmJHBCjMV1NsewphGgA2SbyrVjN7IBcOte6b1HR7JGup2");
                put("code", code);
                put("state", String.valueOf(state));
                put("redirect_uri", redirect_uri);
            }
        }));
        log("Request:", request);

        HttpURLConnection connection = setupConnection(TOKEN_EXCHANGE);

        connection.setDoOutput(true);
        log("Writing request...");
        IOUtils.write(request, connection.getOutputStream(), FileUtil.getCharset());
        log("Done, reading response");

        return connection;
    }

    HttpURLConnection setupInfoConnection(CodeExchangePayload codePayload) throws IOException {
        HttpURLConnection connection = setupConnection(ACCOUNT_INFO);
        connection.setRequestProperty("Authorization", "Bearer " + codePayload.access_token);
        return connection;
    }

    static HttpURLConnection setupConnection(String _url) throws IOException {
        URL url = new URL(_url);

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setUseCaches(false);
        urlConnection.setReadTimeout(U.getReadTimeout());
        urlConnection.setConnectTimeout(U.getConnectionTimeout());

        return urlConnection;
    }

    private final String logPrefix = "[" + getClass().getSimpleName() + "]";
    private void log(Object...o) {
        U.log(logPrefix, o);
    }

    private static class CodeExchangePayload {
        String access_token, token_type, refresh_token;
        int expires_in;

        void checkConsistency() throws AuthException {
            StringUtil.requireNotBlank(access_token, "access_token");
            StringUtil.requireNotBlank(refresh_token, "refresh_token");

            if(!"Bearer".equals(token_type)) {
                throw new IllegalArgumentException("token_type: " + token_type);
            }

            if(expires_in < 0) {
                throw new IllegalArgumentException("expires_in: " + expires_in);
            }
        }
    }
}
