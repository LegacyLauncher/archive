package pw.modder.http;

import net.legacylauncher.util.EHttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

import java.io.IOException;

public class HttpClientUtils {

    private static final Executor SHARED_EXECUTOR = Executor.newInstance(EHttpClient.createRepeatable());

    public static Response execute(Request request) throws IOException {
        return SHARED_EXECUTOR.execute(request);
    }

    private HttpClientUtils() {
    }
}
