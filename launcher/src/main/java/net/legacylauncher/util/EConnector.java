package net.legacylauncher.util;

import lombok.AllArgsConstructor;
import net.legacylauncher.connection.ConnectionInfo;
import net.legacylauncher.connection.UrlConnector;
import org.apache.hc.client5.http.fluent.Executor;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;

import java.io.IOException;
import java.net.URL;

@AllArgsConstructor
public class EConnector implements UrlConnector<EConnection> {
    private final Executor executor;

    public EConnector() {
        this(Executor.newInstance(EHttpClient.getGlobalClient()));
    }

    @Override
    public EConnection connect(ConnectionInfo info) throws IOException {
        URL url = info.getUrl();
        Request request = Request.get(url.toExternalForm());
        Response response = executor.execute(request);
        return new EConnection(url, response);
    }

}
