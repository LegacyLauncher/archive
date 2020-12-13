package ru.turikhay.tlauncher.bootstrap.pasta;

import com.getsentry.raven.event.Event;
import com.getsentry.raven.event.EventBuilder;
import com.getsentry.raven.event.interfaces.ExceptionInterface;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import ru.turikhay.tlauncher.bootstrap.Bootstrap;
import ru.turikhay.tlauncher.bootstrap.exception.ExceptionList;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Pasta {
    private static final String CREATE_PASTE_URL = "https://pasta.tlaun.ch/create/v1?app_key=%s&client=%s";
    private static final String APP_KEY = "MWEmWNwX9HyJbFQb";

    private final String clientId, content;

    public Pasta(String clientId, String content) {
        this.clientId = clientId;
        this.content = content;
    }

    public PastaLink send() throws PastaException {
        URL url = createUrl();
        IOException e = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return makeRequest(url);
            } catch (TooManyRequests tmr) {
                if(attempt < 3) {
                    int waitTime = (attempt > 1? 61 : 31) + new Random().nextInt(10);
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

            OutputStreamWriter writer = new OutputStreamWriter(c.getOutputStream(), Charsets.UTF_8);
            IOUtils.copy(
                    new StringReader(content),
                    writer
            );
            writer.close();

            String response = IOUtils.toString(c.getInputStream(), Charsets.UTF_8);
            if(response.startsWith("http")) {
                return PastaLink.parse(response);
            } else {
                throw new IOException("illegal response: \"" + response + '\"');
            }
        } catch(IOException ioE) {
            if(c == null || c.getErrorStream() == null) {
                throw ioE;
            }
            if(c.getResponseCode() == TooManyRequests.RESPONSE_CODE) {
                throw new TooManyRequests(ioE);
            }
            List<Exception> exceptionList = new ArrayList<Exception>();
            exceptionList.add(ioE);
            String errorMessage = null;
            try {
                errorMessage = IOUtils.toString(c.getErrorStream(), Charsets.UTF_8);
            } catch(IOException e) {
                exceptionList.add(e);
            }
            if(errorMessage != null) {
                throw new IOException("Pasta returned error: \""+ errorMessage +"\"");
            } else {
                throw new IOException("Could not send and read error response from Pasta",
                        new ExceptionList(exceptionList));
            }
        } finally {
            if(c != null) {
                c.disconnect();
            }
        }
    }

    private URL createUrl() {
        String clientId = this.clientId == null? "bootstrap" : this.clientId;
        URL url;
        try {
            url = new URL(
                    String.format(CREATE_PASTE_URL,
                            URLEncoder.encode(APP_KEY, "UTF-8"),
                            URLEncoder.encode(clientId, "UTF-8")
                    )
            );
        } catch(UnsupportedEncodingException e) {
            throw new Error("UTF-8 not supported", e);
        } catch(MalformedURLException e) {
            throw new RuntimeException("couldn't create url", e);
        }
        return url;
    }
}
