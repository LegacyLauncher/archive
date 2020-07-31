package ru.turikhay.tlauncher.user;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.UUID;

import static org.testng.Assert.*;

public class ElyLegacyAuthTest {
    private ElyLegacyAuth auth = new ElyLegacyAuth();

    private ElyLegacyUser auth() throws AuthException, IOException {
        return auth.authorize("mums", "lollolami");
    }

    @Test
    public void testAuthorize() throws Exception {
        ElyLegacyUser user = auth();
        assertNotNull(user, "user");
        assertEquals(user.getUsername(), "mums");
        assertEquals(user.getType(), ElyLegacyUser.TYPE);
        assertFalse(StringUtils.isBlank(user.getDisplayName()), "displayName");
        assertEquals(user.getUUID(), UUID.fromString("9f712936-14e0-57e0-8e6b-99df4715ec6a"));
        assertFalse(StringUtils.isBlank(user.getClientToken()), "clientToken");
        assertFalse(StringUtils.isBlank(user.getAccessToken()), "accessToken");
    }

    @Test
    public void testValidate() throws Exception {
        ElyLegacyUser user = auth();

        String
                oldClientToken = user.getClientToken(),
                oldAccessToken = user.getAccessToken();

        auth.validate(user);

        String
                newClientToken = user.getClientToken(),
                newAccessToken = user.getAccessToken();

        assertEquals(oldClientToken, newClientToken, "oldClientToken != newClientToken");
        assertNotEquals(oldAccessToken, newAccessToken, "oldAccessToken == newAccessToken");
    }

    /*static {

        try {
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, new X509TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            }, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
    }*/

}