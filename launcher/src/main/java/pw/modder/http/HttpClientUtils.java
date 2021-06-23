package pw.modder.http;

import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import ru.turikhay.util.EHttpClient;

import java.io.IOException;

public class HttpClientUtils {

    private static final Executor SHARED_EXECUTOR = Executor.newInstance(EHttpClient.createRepeatable());

    public static Response execute(Request request) throws IOException {
        return SHARED_EXECUTOR.execute(request);
    }

    private HttpClientUtils() {
    }
}
