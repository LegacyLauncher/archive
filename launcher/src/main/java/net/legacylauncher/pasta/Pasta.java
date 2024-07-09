package net.legacylauncher.pasta;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.logger.LogFile;
import net.legacylauncher.util.CharsetData;
import net.legacylauncher.util.CharsetDataHttpEntity;
import net.legacylauncher.util.EHttpClient;
import net.legacylauncher.util.StringCharsetData;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

import static net.legacylauncher.pasta.PastaResult.PastaFailed;
import static net.legacylauncher.pasta.PastaResult.PastaUploaded;

@Slf4j
public class Pasta {
    private static final String CREATE_PASTE_URL = "https://pasta.llaun.ch/create/v1";

    private static final int
            RESPONSE_OK = 201,
            RESPONSE_ERROR_TOO_LONG = 413,
            RESPONSE_ERROR_TOO_MANY_REQUESTS = 429;
    private final ArrayList<PastaListener> listeners;
    private CharsetData data;
    private boolean ignoreTooManyRequests;
    private PastaFormat format = PastaFormat.LOGS;
    private PastaResult result;

    public Pasta() {
        listeners = new ArrayList<>();
    }

    public static String paste(CharsetData data, PastaFormat format) {
        Pasta pasta = new Pasta();
        pasta.setData(data);
        pasta.setIgnoreTooManyRequests();
        pasta.setFormat(format);
        PastaResult result = pasta.paste();
        if (result instanceof PastaUploaded) {
            return ((PastaUploaded) result).getURL().toExternalForm();
        } else if (result instanceof PastaFailed) {
            return "pasta: " + ((PastaFailed) result).getError().toString();
        } else {
            return "pasta: not available";
        }
    }

    public static String paste(String data, PastaFormat format) {
        if (data == null) {
            return "pasta: input null";
        } else if (StringUtils.isBlank(data)) {
            return "pasta: input blank";
        }
        return paste(new StringCharsetData(data), format);
    }

    public static String pasteJson(String json) {
        return paste(json, PastaFormat.JSON);
    }

    public static String pasteFile(File file, PastaFormat format, Charset charset) {
        if (file == null) {
            return "pasta: file null";
        }
        if (!file.isFile()) {
            return "pasta: not a file: " + file.getAbsolutePath();
        }
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }
        return paste(new LogFile(file, charset), format);
    }

    public static String pasteFile(File file, PastaFormat format) {
        return pasteFile(file, format, null);
    }

    public static String pasteFile(File file) {
        return pasteFile(file, PastaFormat.PLAIN);
    }

    public void setIgnoreTooManyRequests() {
        this.ignoreTooManyRequests = true;
    }

    public void setData(CharsetData data) {
        this.data = data;
    }

    public void setFormat(PastaFormat format) {
        this.format = format;
    }

    public void addListener(PastaListener listener) {
        listeners.add(listener);
    }

    public PastaResult getResult() {
        return result;
    }

    public PastaResult paste() {
        for (PastaListener l : listeners) {
            l.pasteUploading(this);
        }

        try {
            result = doPaste();
        } catch (Throwable e) {
            log.error("Could not upload paste", e);
            result = new PastaFailed(this, e);
        }

        for (PastaListener l : listeners) {
            l.pasteDone(this);
        }

        return result;
    }

    private PastaUploaded doPaste() throws IOException {
        final CharsetData data = this.data;
        final boolean ignoreTmr = this.ignoreTooManyRequests;
        final PastaFormat format = this.format;

        if (data == null) {
            throw new NullPointerException("data");
        }

        long length = data.length();
        if (length == 0) {
            throw new RuntimeException("data is empty");
        }

        try (CloseableHttpClient httpClient = EHttpClient.createRepeatable()) {
            PastaUploaded result = null;
            for (int attempt = 1; attempt <= 2; attempt++) {
                try {
                    result = makeRequest(httpClient, data, format);
                } catch (TooManyRequests tmr) {
                    if (ignoreTmr) {
                        throw tmr;
                    }
                    int waitTime = (attempt > 1 ? 61 : 31) + new Random().nextInt(10);
                    log.warn("Pasta could not be sent because of the rate limit (attempt {}, wait time {}s)", attempt, waitTime);
                    try {
                        Thread.sleep(waitTime * 1000L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException("interrupted", e);
                    }
                    continue;
                }
                break;
            }
            if (result == null) {
                throw new TooManyRequests();
            }
            log.info("Pasta has been sent successfully: {}", result.getURL());
            return result;
        }
    }

    private PastaUploaded makeRequest(HttpClient httpClient, CharsetData data, PastaFormat format)
            throws IOException {
        HttpPost httpPost = new HttpPost(CREATE_PASTE_URL);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, format.getContentType().withCharset(StandardCharsets.UTF_8).toString());
        httpPost.setEntity(new CharsetDataHttpEntity(data));
        return httpClient.execute(httpPost, (response) -> {
            int statusCode = response.getCode();
            switch (statusCode) {
                case RESPONSE_OK:
                    return readSuccess(response);
                case RESPONSE_ERROR_TOO_LONG:
                    throw new PastaTooLong(data.length());
                case RESPONSE_ERROR_TOO_MANY_REQUESTS:
                    throw new TooManyRequests();
            }
            throw new PastaUnavailable(
                    statusCode,
                    EntityUtils.toString(response.getEntity())
            );
        });
    }

    private PastaUploaded readSuccess(ClassicHttpResponse response) throws IOException, ParseException {
        String result = EntityUtils.toString(response.getEntity());
        if (result.startsWith("http")) {
            URL urlLink;
            try {
                urlLink = new URL(result);
            } catch (MalformedURLException e) {
                urlLink = null;
            }
            if (urlLink != null) { // not malformed
                return new PastaUploaded(this, urlLink);
            }
        }
        throw new PastaUnavailable(response.getCode(), result);
    }

    private String getSample() {
        try (Reader reader = data.read(); StringWriter writer = new StringWriter()) {
            char[] buffer = new char[256];
            int read = reader.read(buffer);
            if (read > 0) {
                writer.write(buffer, 0, read);
            } else {
                return "empty sample";
            }
            return writer.toString();
        } catch (IOException e) {
            log.warn("Error reading sample", e);
            return "couldn't get sample: " + e;
        }
    }
}
