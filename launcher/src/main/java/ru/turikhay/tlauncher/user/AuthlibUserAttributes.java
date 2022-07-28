package ru.turikhay.tlauncher.user;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Locale;

public class AuthlibUserAttributes {
    public static void validateUserAttributes(String accessToken) throws IOException, AuthException {
        HttpResponse response = Request.Get("https://api.minecraftservices.com/player/attributes")
                .addHeader("Authorization", "Bearer " + accessToken)
                .execute()
                .returnResponse();
        String body = EntityUtils.toString(response.getEntity());
        int statusCode = response.getStatusLine().getStatusCode();
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
