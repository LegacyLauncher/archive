package ru.turikhay.tlauncher.pasta;

import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import static ru.turikhay.tlauncher.pasta.PastaResult.*;

public class Pasta {
    private static final Logger LOGGER = LogManager.getLogger(Pasta.class);

    private static final String APP_KEY = "kByB9b8MdAbgMq66";
    private static final String CREATE_PASTE_URL = "https://pasta.tlaun.ch/create/v1?app_key=%s&client=%s&format=%s";

    private CharsetData data;
    private boolean ignoreTooManyRequests;
    private PastaFormat format = PastaFormat.LOGS;

    private final ArrayList<PastaListener> listeners;
    private PastaResult result;

    public Pasta() {
        listeners = new ArrayList<>();
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
            LOGGER.error("Could not upload paste", e);
            if(!(e instanceof TooManyRequests)) {
                Sentry.capture(new EventBuilder()
                        .withMessage("pasta not sent")
                        .withExtra("sample", getSample())
                        .withLevel(Event.Level.ERROR)
                        .withSentryInterface(new ExceptionInterface(e))
                );
            }
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

        String clientId;
        if(TLauncher.getInstance() != null) {
            clientId = TLauncher.getInstance().getSettings().getClient().toString();
        } else {
            clientId = "test";
        }

        URL url = U.makeURL(
                String.format(java.util.Locale.ROOT, CREATE_PASTE_URL,
                        UrlEncoder.encode(APP_KEY),
                        UrlEncoder.encode(clientId),
                        UrlEncoder.encode(format.value())
                )
        );

        PastaUploaded result = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                result = makeRequest(url, data);
                break;
            } catch(TooManyRequests tmr) {
                if(ignoreTmr) {
                    throw tmr;
                }
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

    private PastaUploaded makeRequest(URL url, CharsetData data) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection(U.getProxy());
            connection.setConnectTimeout(U.getConnectionTimeout());
            connection.setReadTimeout(U.getReadTimeout());
            connection.setRequestProperty("Content-Type", "text/plain; charset=\""+ FileUtil.getCharset().name() +"\"");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            try(Reader input = data.read();
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
                switch (connection.getResponseCode()) {
                    case 413:
                        throw new PastaTooLong(data.length());
                    case 429:
                        throw new TooManyRequests(ioE);
                }
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

    private String getSample() {
        try(Reader reader = data.read(); StringWriter writer = new StringWriter()){
            char[] buffer = new char[256];
            int read = reader.read(buffer);
            if(read > 0) {
                writer.write(buffer, 0, read);
            } else {
                return "empty sample";
            }
            return writer.toString();
        } catch (IOException e) {
            LOGGER.warn("Error reading sample", e);
            return "couldn't get sample: " + e.toString();
        }
    }

    public static String paste(CharsetData data, PastaFormat format) {
        Pasta pasta = new Pasta();
        pasta.setData(data);
        pasta.setIgnoreTooManyRequests();
        pasta.setFormat(format);
        PastaResult result = pasta.paste();
        if(result instanceof PastaUploaded) {
            return ((PastaUploaded) result).getURL().toExternalForm();
        } else if (result instanceof PastaFailed) {
            return "pasta: " + ((PastaFailed) result).getError().toString();
        } else {
            return "pasta: not available";
        }
    }

    public static String paste(String data, PastaFormat format) {
        if(data == null) {
            return "pasta: input null";
        } else if(StringUtils.isBlank(data)) {
            return "pasta: input blank";
        }
        return paste(new StringCharsetData(data), format);
    }

    public static String pasteJson(String json) {
        return paste(json, PastaFormat.JSON);
    }
}
