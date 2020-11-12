package ru.turikhay.tlauncher.pasta;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CharSequenceReader;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.UrlEncoder;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import static ru.turikhay.tlauncher.pasta.PastaResult.*;

public class Pasta {
    private static final String APP_KEY = "kByB9b8MdAbgMq66";
    private static final String CREATE_PASTE_URL = "https://pasta.tlaun.ch/create/v1?app_key=%s&client=%s";

    private CharSequence content;

    private final ArrayList<PastaListener> listeners;
    private PastaResult result;

    public Pasta() {
        listeners = new ArrayList<>();
    }

    public final CharSequence getContent() {
        return content;
    }

    public final void setContent(CharSequence content) {
        this.content = content;
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
        } catch (Throwable var3) {
            U.log("Could not upload paste", var3);
            result = new PastaFailed(this, var3);
        }

        for (PastaListener l : listeners) {
            l.pasteDone(this);
        }

        return result;
    }

    private PastaUploaded doPaste() throws IOException {
        CharSequence contentSequence = getContent();

        if (StringUtils.isEmpty(contentSequence)) {
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
                result = makeRequest(url, contentSequence);
                break;
            } catch(TooManyRequests tmr) {
                int waitTime = (attempt > 1? 61 : 31) + new Random().nextInt(10);
                U.log("Pasta could not be sent because of the rate limit (attempt "+ attempt +", "+ waitTime +"s)");
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

        U.log("Pasta sent:", result.getURL());
        return result;
    }

    private PastaUploaded makeRequest(URL url, CharSequence contentSequence) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection(U.getProxy());
            connection.setConnectTimeout(U.getConnectionTimeout());
            connection.setReadTimeout(U.getReadTimeout());
            connection.setRequestProperty("Content-Type", "text/plain; charset=\""+ FileUtil.getCharset().name() +"\"");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            try(CharSequenceReader input = new CharSequenceReader(contentSequence);
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
