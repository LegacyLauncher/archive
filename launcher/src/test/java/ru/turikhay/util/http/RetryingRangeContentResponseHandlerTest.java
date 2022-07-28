package ru.turikhay.util.http;

import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.ByteArrayBuffer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RetryingRangeContentResponseHandlerTest {

    @Test
    void test() throws IOException {
        final String url = "https://cdn.turikhay.ru/tlauncher/legacy_beta/bootstrap.json";
        Executor executor = Executor.newInstance();

        Request request0 = Request.Get(url);
        String result0 = executor.execute(request0).returnContent().asString();

        Request request1 = Request.Get(url);
        RetryingRangeContentResponseHandler handler = new OneChunkPerTime(request1, executor);
        String result1 = executor.execute(request1).handleResponse(handler).asString();
        assertEquals(result0, result1, "normal and chunked downloads don't match");
    }

    private static class OneChunkPerTime extends RetryingRangeContentResponseHandler {
        public OneChunkPerTime(Request request, Executor executor) {
            super(request, executor, false);
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