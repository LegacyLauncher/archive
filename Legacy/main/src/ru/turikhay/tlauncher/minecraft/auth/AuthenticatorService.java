package ru.turikhay.tlauncher.minecraft.auth;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.U;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

class AuthenticatorService {
    private static void log(Object... o) {
        U.log("[AUTHSERV]", o);
    }

    private static void debug(Object... o) {
        if (TLauncher.getInstance() != null && TLauncher.getInstance().getDebug()) log(o);
    }

    private static HttpURLConnection createUrlConnection(URL url) throws IOException {
        Validate.notNull(url);
        debug("Opening connection to " + url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(U.getProxy());
        connection.setConnectTimeout(U.getConnectionTimeout());
        connection.setReadTimeout(U.getReadTimeout());
        connection.setUseCaches(false);
        return connection;
    }

    public static String performPostRequest(URL url, String post, String contentType) throws IOException {
        Validate.notNull(url);
        Validate.notNull(post);
        Validate.notNull(contentType);
        HttpURLConnection connection = createUrlConnection(url);
        byte[] postAsBytes = post.getBytes(Charsets.UTF_8);
        connection.setRequestProperty("Content-Type", contentType + "; charset=utf-8");
        connection.setRequestProperty("Content-Length", "" + postAsBytes.length);
        connection.setDoOutput(true);
        debug("Writing POST data to " + url + ": " + post);
        OutputStream outputStream = null;

        try {
            outputStream = connection.getOutputStream();
            IOUtils.write(postAsBytes, outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

        debug("Reading data from " + url);
        InputStream inputStream = null;

        String var10;
        try {
            inputStream = connection.getInputStream();
            String e = IOUtils.toString(inputStream, Charsets.UTF_8);
            debug("Successful read, server response was " + connection.getResponseCode());
            debug("Response: " + e);
            var10 = e;
        } catch (IOException var18) {
            IOUtils.closeQuietly(inputStream);
            inputStream = connection.getErrorStream();
            if (inputStream == null) {
                debug("Request failed", var18);
                throw var18;
            }

            debug("Reading error page from " + url);
            String result = IOUtils.toString(inputStream, Charsets.UTF_8);
            debug("Successful read, server response was " + connection.getResponseCode());
            debug("Response: " + result);
            var10 = result;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        return var10;
    }

    public static String performGetRequest(URL url) throws IOException {
        Validate.notNull(url);
        HttpURLConnection connection = createUrlConnection(url);
        debug("Reading data from " + url);
        InputStream inputStream = null;

        String var6;
        try {
            try {
                inputStream = connection.getInputStream();
                String e = IOUtils.toString(inputStream, Charsets.UTF_8);
                debug("Successful read, server response was " + connection.getResponseCode());
                debug("Response: " + e);
                var6 = e;
                return var6;
            } catch (IOException var9) {
                IOUtils.closeQuietly(inputStream);
                inputStream = connection.getErrorStream();
                if (inputStream == null) {
                    debug("Request failed", var9);
                    throw var9;
                }
            }

            debug("Reading error page from " + url);
            String result = IOUtils.toString(inputStream, Charsets.UTF_8);
            debug("Successful read, server response was " + connection.getResponseCode());
            debug("Response: " + result);
            var6 = result;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        return var6;
    }

    public static URL constantURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException var2) {
            throw new Error("Couldn\'t create constant for " + url, var2);
        }
    }

    public static String buildQuery(Map<String, Object> query) {
        if (query == null) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder();
            Iterator var3 = query.entrySet().iterator();

            while (var3.hasNext()) {
                Entry entry = (Entry) var3.next();
                if (builder.length() > 0) {
                    builder.append('&');
                }

                try {
                    builder.append(URLEncoder.encode((String) entry.getKey(), "UTF-8"));
                } catch (UnsupportedEncodingException var6) {
                    log("Unexpected exception building query", var6);
                }

                if (entry.getValue() != null) {
                    builder.append('=');

                    try {
                        builder.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
                    } catch (UnsupportedEncodingException var5) {
                        log("Unexpected exception building query", var5);
                    }
                }
            }

            return builder.toString();
        }
    }

    public static URL concatenateURL(URL url, String query) {
        try {
            return url.getQuery() != null && url.getQuery().length() > 0 ? new URL(url.getProtocol(), url.getHost(), url.getFile() + "&" + query) : new URL(url.getProtocol(), url.getHost(), url.getFile() + "?" + query);
        } catch (MalformedURLException var3) {
            throw new IllegalArgumentException("Could not concatenate given URL with GET arguments!", var3);
        }
    }
}
