package net.legacylauncher.user;


import lombok.SneakyThrows;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.util.Locale;

public class AuthlibUserAttributes {
    public static void validateUserAttributes(String accessToken) throws IOException, AuthException {
        Request.get("https://api.minecraftservices.com/player/attributes")
                .addHeader("Authorization", "Bearer " + accessToken)
                .execute()
                .handleResponse(response -> {
                    handleResponse(response);
                    return null;
                });
    }

    @SneakyThrows(AuthException.class)
    private static void handleResponse(ClassicHttpResponse response) throws IOException, ParseException {
        String body = EntityUtils.toString(response.getEntity());
        int statusCode = response.getCode();
        switch (statusCode) {
            case 200:
                return;
            case 403:
                throw new MigrationRequiredException();
            default:
                throw new AuthUnknownException(new IOException(String.format(Locale.ROOT,
                        "bad status code: %d (%s)", statusCode, body
                )));
        }
    }
}
