package ru.turikhay.tlauncher.user;

import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.response.Response;
import ru.turikhay.tlauncher.managers.McleaksManager;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class McleaksAuth extends AuthlibAuth<McleaksUser> {
    public McleaksUser authorize(String altToken) throws AuthException, IOException {
        checkAvailable();
        String clientToken = randomClientToken();
        com.mojang.authlib.UserAuthentication userAuthentication;
        try {
            userAuthentication = super.authorize(clientToken, altToken, "12345");
        } catch (InvalidCredentialsException inv) {
            throw new McleaksAltTokenExpired();
        }
        return new McleaksUser(altToken, clientToken, userAuthentication);
    }

    @Override
    protected void logIn(UserAuthentication userAuthentication) throws AuthException, IOException {
        checkAvailable();
        try {
            super.logIn(userAuthentication);
        } catch (InvalidCredentialsException inv) {
            if (inv.getMessage() != null && inv.getMessage().contains("ALT-TOKEN")) {
                throw new McleaksAltTokenExpired();
            }
        }
    }

    @Override
    public void validate(McleaksUser user) throws AuthException, IOException {
        checkAvailable();
        super.validate(user);
    }

    protected com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService createYggdrasilAuthenticationService(String clientToken) {
        return new com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService(U.getProxy(), StringUtil.requireNotBlank(clientToken)) {
            @Override
            protected <T extends Response> T makeRequest(final URL url, Object input, Class<T> classOfT) throws AuthenticationException {
                URL newUrl;
                if (url.getProtocol().equals("https")) {
                    try {
                        // open connection and tweak it
                        final HttpsURLConnection connection = McleaksManager.getConnector().setupHttpsConnection(url);
                        // replace URLStreamHandler to make it use tweaked connection
                        newUrl = new URL(null, url.toExternalForm(), new URLStreamHandler() {
                            @Override
                            protected URLConnection openConnection(URL u) {
                                return connection;
                            }

                            protected URLConnection openConnection(URL u, Proxy p) {
                                return connection;
                            }
                        });
                    } catch (IOException ioE) {
                        throw new AuthenticationUnavailableException(ioE);
                    }
                } else {
                    newUrl = url;
                }
                return super.makeRequest(newUrl, input, classOfT);
            }
        };
    }

    private static void checkAvailable() throws AuthException {
        if (McleaksManager.isUnsupported()) {
            throw new AuthException("MCLeaks is unsupported", "mcleaks.unsupported");
        }
        if (!McleaksManager.getStatus().hasStatus()) {
            throw new AuthException("MCLeaks status is unknown", "mcleaks.status-unknown");
        }
        if (McleaksManager.getStatus().getServerIp() == null) {
            throw new AuthException("MCLeaks is unavailable", "mcleaks.unavailable");
        }
    }
}
