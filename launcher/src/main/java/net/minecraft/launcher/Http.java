package net.minecraft.launcher;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Http {
    private static String buildQuery(Map<String, Object> query) {
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
                U.log("Unexpected exception building query", var6);
            }

            if (entry.getValue() != null) {
                builder.append('=');

                try {
                    builder.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
                } catch (UnsupportedEncodingException var5) {
                    U.log("Unexpected exception building query", var5);
                }
            }
        }

        return builder.toString();
    }

    public static String performPost(URL url, Map<String, Object> query) throws IOException {
        return performPost(url, buildQuery(query), "application/x-www-form-urlencoded");
    }

    public static String performGet(URL url, int connTimeout, int readTimeout) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(U.getProxy());
        connection.setConnectTimeout(connTimeout);
        connection.setReadTimeout(readTimeout);
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }

        reader.close();
        return response.toString();
    }

    public static String performGet(String url) throws IOException {
        return performGet(constantURL(url), U.getConnectionTimeout(), U.getReadTimeout());
    }

    public static String performGet(URL url) throws IOException {
        return performGet(url, U.getConnectionTimeout(), U.getReadTimeout());
    }

    public static URL concatenateURL(URL url, String args) throws MalformedURLException {
        return url.getQuery() != null && url.getQuery().length() > 0 ? new URL(url.getProtocol(), url.getHost(), url.getFile() + "?" + args) : new URL(url.getProtocol(), url.getHost(), url.getFile() + "&" + args);
    }

    public static String performPost(URL url, String parameters, String contentType) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection(U.getProxy());
        byte[] paramAsBytes = parameters.getBytes(Charset.forName("UTF-8"));
        connection.setConnectTimeout(U.getConnectionTimeout());
        connection.setReadTimeout(U.getReadTimeout());
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", contentType + "; charset=utf-8");
        connection.setRequestProperty("Content-Length", "" + paramAsBytes.length);
        connection.setRequestProperty("Content-Language", "en-US");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
        writer.write(paramAsBytes);
        writer.flush();
        writer.close();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }

        reader.close();
        return response.toString();
    }

    public static URL constantURL(String input) {
        try {
            return new URL(input);
        } catch (MalformedURLException var2) {
            throw new Error(var2);
        }
    }

    public static String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20").replaceAll("\\%3A", ":").replaceAll("\\%2F", "/").replaceAll("\\%21", "!").replaceAll("\\%27", "\'").replaceAll("\\%28", "(").replaceAll("\\%29", ")").replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException var2) {
            throw new RuntimeException("UTF-8 is not supported.", var2);
        }
    }

    private static void log(Object... o) {
        U.log("[AUTHSERV]", o);
    }

    private static void debug(Object... o) {
        if (TLauncher.getInstance() != null && TLauncher.getInstance().isDebug()) log(o);
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
}
