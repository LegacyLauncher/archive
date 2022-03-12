package ru.turikhay.tlauncher.jre;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.repository.RepositoryProxy;
import ru.turikhay.util.async.AsyncThread;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class JavaRuntimeRemoteListFetcher {
    private static final Logger LOGGER = LogManager.getLogger(JavaRuntimeRemoteListFetcher.class);

    private Future<JavaRuntimeRemoteList> list;

    public Future<JavaRuntimeRemoteList> fetch() {
        if (list == null) {
            list = AsyncThread.future(this::doFetch);
        }
        return list;
    }

    public JavaRuntimeRemoteList fetchNow() throws ExecutionException, InterruptedException {
        return fetch().get();
    }

    private JavaRuntimeRemoteList doFetch() throws Exception {
        try {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(JavaRuntimeRemoteList.class, new JavaRuntimeRemoteListDeserializer())
                    .registerTypeAdapter(Date.class, new DateTypeAdapter(false))
                    .create();
            String content = RepositoryProxy.requestMaybeProxy(JavaRuntimeRemoteList.URL);
            JavaRuntimeRemoteList remoteList = gson.fromJson(content, JavaRuntimeRemoteList.class);
            return Objects.requireNonNull(remoteList, "remoteList");
        } catch (Exception e) {
            LOGGER.warn("Failed", e);
            Sentry.capture(new EventBuilder()
                    .withLevel(Event.Level.ERROR)
                    .withMessage("couldn't fetch remote runtime list")
                    .withSentryInterface(new ExceptionInterface(e))
            );
            throw e;
        }
    }
}
