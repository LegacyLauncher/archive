package net.minecraft.launcher;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.U;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class Http {
    private static final Logger LOGGER = LogManager.getLogger();

    public static URL constantURL(String input) {
        try {
            return new URL(input);
        } catch (MalformedURLException var2) {
            throw new Error(var2);
        }
    }

    public static String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20").replaceAll("%3A", ":").replaceAll("%2F", "/").replaceAll("%21", "!").replaceAll("%27", "'").replaceAll("%28", "(").replaceAll("%29", ")").replaceAll("%7E", "~");
        } catch (UnsupportedEncodingException var2) {
            throw new RuntimeException("UTF-8 is not supported.", var2);
        }
    }

    private static HttpURLConnection createUrlConnection(URL url) throws IOException {
        Validate.notNull(url);
        LOGGER.trace("Opening connection to {}", url);
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
        LOGGER.trace("Writing POST data to {}: {}", url, post);
        OutputStream outputStream = null;

        try {
            outputStream = connection.getOutputStream();
            IOUtils.write(postAsBytes, outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

        LOGGER.trace("Reading data from {}", url);
        InputStream inputStream = null;

        String var10;
        try {
            inputStream = connection.getInputStream();
            String e = IOUtils.toString(inputStream, Charsets.UTF_8);
            LOGGER.trace("Successful read, server response was {}", connection.getResponseCode());
            LOGGER.trace("Response: {}", e);
            var10 = e;
        } catch (IOException var18) {
            IOUtils.closeQuietly(inputStream);
            inputStream = connection.getErrorStream();
            if (inputStream == null) {
                LOGGER.error("Request to {} failed", url, var18);
                throw var18;
            }

            LOGGER.error("Reading error page from {}", url);
            String result = IOUtils.toString(inputStream, Charsets.UTF_8);
            LOGGER.error("Successful read, server response was {}", connection.getResponseCode());
            LOGGER.error("Response: {}", result);
            var10 = result;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        return var10;
    }
}
