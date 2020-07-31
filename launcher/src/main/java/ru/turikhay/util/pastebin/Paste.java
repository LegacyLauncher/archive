package ru.turikhay.util.pastebin;

import net.minecraft.launcher.Http;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.UrlEncoder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class Paste {
    private static final String DEV_KEY = "19a886bbdf6e11670d7f0a4e2dace1a5";
    private static final URL POST_URL = Http.constantURL("https://pastebin.com/api/api_post.php");
    private String title;
    private CharSequence content;
    private String format;
    private ExpireDate expires;
    private Visibility visibility;
    private final ArrayList<PasteListener> listeners;
    private PasteResult result;

    public Paste() {
        expires = ExpireDate.ONE_WEEK;
        visibility = Visibility.NOT_LISTED;
        listeners = new ArrayList<PasteListener>();
    }

    public final String getTitle() {
        return title;
    }

    public final void setTitle(String title) {
        this.title = title;
    }

    public final CharSequence getContent() {
        return content;
    }

    public final void setContent(CharSequence content) {
        this.content = content;
    }

    public final String getFormat() {
        return format;
    }

    public final void setFormat(String format) {
        this.format = format;
    }

    public final ExpireDate getExpireDate() {
        return expires;
    }

    public final void setExpireDate(ExpireDate date) {
        expires = date;
    }

    public final Visibility getVisibility() {
        return visibility;
    }

    public final void setVisibility(Visibility vis) {
        visibility = vis;
    }

    public void addListener(PasteListener listener) {
        listeners.add(listener);
    }

    public void removeListener(PasteListener listener) {
        listeners.remove(listener);
    }

    public PasteResult getResult() {
        return result;
    }

    public PasteResult paste() {
        for (PasteListener l : listeners) {
            l.pasteUploading(this);
        }

        try {
            result = doPaste();
        } catch (Throwable var3) {
            U.log("Could not upload paste", var3);
            result = new PasteResult.PasteFailed(this, var3);
        }

        for (PasteListener l : listeners) {
            l.pasteDone(this);
        }

        return result;
    }

    private PasteResult.PasteUploaded doPaste() throws IOException {
        if (StringUtils.isEmpty(getContent())) {
            throw new IllegalArgumentException("content is empty");
        } else if (getVisibility() == null) {
            throw new NullPointerException("visibility");
        } else if (getExpireDate() == null) {
            throw new NullPointerException("expire date");
        }

        HashMap<String, String> query = new HashMap<String, String>();
        query.put("api_dev_key", DEV_KEY);
        query.put("api_option", "paste");
        query.put("api_paste_name", getTitle());
        query.put("api_paste_private", String.valueOf(getVisibility().getValue()));
        query.put("api_paste_expire_date", getExpireDate().getValue());

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) POST_URL.openConnection(U.getProxy());
            connection.setConnectTimeout(U.getConnectionTimeout());
            connection.setReadTimeout(U.getReadTimeout());

            connection.setDoOutput(true); // send our data
            OutputStream output = connection.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(output);
            for (HashMap.Entry<String, String> entry : query.entrySet()) { // sending metadata
                writer.write(UrlEncoder.encode(entry.getKey()));
                writer.write("=");
                writer.write(StringUtils.isEmpty(entry.getValue()) ? "" : UrlEncoder.encode(entry.getValue()));
                writer.write("&");
            }

            writer.write("api_paste_code");
            writer.write("=");
            writer.flush();

            UrlEncoder.Encoder contentEncoder = new UrlEncoder(getContent()).getEncoder();
            IOUtils.copy(contentEncoder, output); // sending content

            output.close(); // we don't need to send anything else; let the server respond

            String response = IOUtils.toString(new InputStreamReader(connection.getInputStream(), FileUtil.DEFAULT_CHARSET));
            if (response.startsWith("http")) {
                return new PasteResult.PasteUploaded(this, new URL(response));
            } else {
                throw new IOException("illegal response: \"" + response + '\"');
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
