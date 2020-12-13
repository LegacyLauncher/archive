package ru.turikhay.tlauncher.pasta;

import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.logger.LogFile;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.UrlEncoder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import static ru.turikhay.tlauncher.pasta.PastaResult.*;

public class Pasta {
    private static final Logger LOGGER = LogManager.getLogger(Pasta.class);

    private static final String APP_KEY = "kByB9b8MdAbgMq66";
    private static final String CREATE_PASTE_URL = "https://pasta.tlaun.ch/create/v1?app_key=%s&client=%s";

    private LogFile logFile;

    private final ArrayList<PastaListener> listeners;
    private PastaResult result;

    public Pasta() {
        listeners = new ArrayList<>();
    }

    public void setLogFile(LogFile logFile) {
        this.logFile = logFile;
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
            LOGGER.error("Could not upload paste", e);
            Sentry.capture(new EventBuilder()
                .withMessage("pasta not sent")
                .withLevel(Event.Level.ERROR)
                .withSentryInterface(new ExceptionInterface(e))
            );
            result = new PastaFailed(this, e);
        }

        for (PastaListener l : listeners) {
            l.pasteDone(this);
        }

        return result;
    }

    private PastaUploaded doPaste() throws IOException {
        final LogFile logFile = this.logFile;

        if (logFile == null || !logFile.exists()) {
            throw new IllegalArgumentException("content is empty");
        }

        String clientId;
        if(TLauncher.getInstance() != null) {
            clientId = TLauncher.getInstance().getSettings().getClient().toString();
        } else {
            clientId = "test";
        }

        URL url = U.makeURL(
                String.format(CREATE_PASTE_URL,
                        UrlEncoder.encode(APP_KEY),
                        UrlEncoder.encode(clientId)
                )
        );

        PastaUploaded result = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                result = makeRequest(url, logFile);
                break;
            } catch(TooManyRequests tmr) {
                int waitTime = (attempt > 1? 61 : 31) + new Random().nextInt(10);
                LOGGER.warn("Pasta could not be sent because of the rate limit (attempt {}, wait time {}s", attempt, waitTime);
                try {
                    Thread.sleep(waitTime * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException("interrupted", e);
                }
            }
        }

        if(result == null) {
            throw new TooManyRequests();
        }

        LOGGER.info("Pasta has been sent successfully: {}", result.getURL());
        return result;
    }

    private PastaUploaded makeRequest(URL url, LogFile logFile) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection(U.getProxy());
            connection.setConnectTimeout(U.getConnectionTimeout());
            connection.setReadTimeout(U.getReadTimeout());
            connection.setRequestProperty("Content-Type", "text/plain; charset=\""+ FileUtil.getCharset().name() +"\"");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            try(InputStreamReader input = logFile.read();
                OutputStreamWriter output = new OutputStreamWriter(
                        connection.getOutputStream(),
                        FileUtil.getCharset()
                );
            ) {
                IOUtils.copy(input, output);
            }

            String response = IOUtils.toString(connection.getInputStream(), FileUtil.getCharset());
            if (response.startsWith("http")) {
                return new PastaUploaded(this, new URL(response));
            } else {
                throw new IOException("illegal response: \"" + response + '\"');
            }
        } catch(IOException ioE) {
            if(connection != null && connection.getErrorStream() != null) {
                if(connection.getResponseCode() == 429) {
                    throw new TooManyRequests(ioE);
                }
                String error = null;
                try {
                    error = IOUtils.toString(connection.getErrorStream(), FileUtil.getCharset());
                } catch(IOException e) {
                    ioE.addSuppressed(e);
                }
                if(error != null) {
                    throw new IOException("error: " + error);
                }
            }
            throw ioE;
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }
}
