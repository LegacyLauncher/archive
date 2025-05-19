package net.legacylauncher.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.legacylauncher.connection.Connection;
import org.apache.hc.client5.http.fluent.Response;

import java.net.URL;

@AllArgsConstructor
@Getter
public class EConnection implements Connection {
    private final URL url;
    private final Response response;

    @Override
    public void disconnect() {
        response.discardContent();
    }
}
