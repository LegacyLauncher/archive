package ru.turikhay.tlauncher.bootstrap.pasta;

import com.getsentry.raven.event.Event;
import com.getsentry.raven.event.EventBuilder;
import com.getsentry.raven.event.interfaces.ExceptionInterface;
import org.apache.commons.io.IOUtils;
import ru.turikhay.tlauncher.bootstrap.Bootstrap;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Pasta {
    private static final String CREATE_PASTE_URL = "https://pasta.llaun.ch/create/v1";

    private final String content;

    public Pasta(String content) {
        this.content = content;
    }

    public PastaLink send() throws PastaException {
        URL url = createUrl();
        IOException e = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return makeRequest(url);
            } catch (TooManyRequests tmr) {
                if (attempt < 3) {
                    int waitTime = (attempt > 1 ? 61 : 31) + new Random().nextInt(10);
                    try {
                        Thread.sleep(waitTime * 1000L);
                    } catch (InterruptedException interrupted) {
                        throw new RuntimeException("interrupted", interrupted);
                    }
                }
                e = tmr;
            } catch (IOException ioE) {
                Bootstrap.SENTRY.sendEvent(new EventBuilder()
                        .withLevel(Event.Level.ERROR)
                        .withMessage("pasta not sent")
                        .withSentryInterface(new ExceptionInterface(ioE))
                );
                e = ioE;
            }
        }
        throw new PastaException("Exhausted attempts limit", e);
    }

    private PastaLink makeRequest(URL url) throws IOException, PastaException {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) url.openConnection(U.getProxy());
            c.setRequestProperty("Content-Type", "text/plain; charset=\"UTF-8\"");
            c.setRequestMethod("POST");
            c.setDoOutput(true);

            OutputStreamWriter writer = new OutputStreamWriter(c.getOutputStream(), StandardCharsets.UTF_8);
            IOUtils.copy(
                    new StringReader(content),
                    writer
            );
            writer.close();

            String response = IOUtils.toString(c.getInputStream(), StandardCharsets.UTF_8);
            if (response.startsWith("http")) {
                return PastaLink.parse(response);
            } else {
                throw new IOException("illegal response: \"" + response + '\"');
            }
        } catch (IOException ioE) {
            if (c == null || c.getErrorStream() == null) {
                throw ioE;
            }
            if (c.getResponseCode() == TooManyRequests.RESPONSE_CODE) {
                throw new TooManyRequests(ioE);
            }
            String errorMessage = null;
            try {
                errorMessage = IOUtils.toString(c.getErrorStream(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                ioE.addSuppressed(e);
            }
            if (errorMessage != null) {
                throw new IOException("Pasta returned error: \"" + errorMessage + "\"");
            } else {
                throw new IOException("Could not send and read error response from Pasta", ioE);
            }
        } finally {
            if (c != null) {
                c.disconnect();
            }
        }
    }

    private URL createUrl() {
        URL url;
        try {
            url = new URL(CREATE_PASTE_URL);
        } catch (MalformedURLException e) {
            throw new RuntimeException("couldn't create url", e);
        }
        return url;
    }
}
