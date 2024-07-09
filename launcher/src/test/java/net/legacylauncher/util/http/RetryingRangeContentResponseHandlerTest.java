package net.legacylauncher.util.http;

import org.apache.hc.client5.http.fluent.Executor;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.util.ByteArrayBuffer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RetryingRangeContentResponseHandlerTest {

    @Test
    @Disabled
    void test() throws IOException {
        final String url = "https://cdn.turikhay.ru/tlauncher/legacy_beta/bootstrap.json";
        Executor executor = Executor.newInstance();

        Request request0 = Request.get(url);
        String result0 = executor.execute(request0).returnContent().asString();

        Request request1 = Request.get(url);
        RetryingRangeContentResponseHandler handler = new OneChunkPerTime(request1, executor);
        String result1 = executor.execute(request1).handleResponse(handler).asString();
        assertEquals(result0, result1, "normal and chunked downloads don't match");
    }

    private static class OneChunkPerTime extends RetryingRangeContentResponseHandler {
        public OneChunkPerTime(Request request, Executor executor) {
            super(request, executor);
        }

        @Override
        long readBuffer(InputStream content, ByteArrayBuffer buffer) throws IOException {
            // read one small chunk per connection
            final byte[] tmp = new byte[32768];
            int r = content.read(tmp);
            if (r > 0) {
                buffer.append(tmp, 0, r);
                return r;
            }
            return 0;
        }
    }

}
