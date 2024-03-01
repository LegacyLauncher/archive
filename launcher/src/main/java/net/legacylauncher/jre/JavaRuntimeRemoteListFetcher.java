package net.legacylauncher.jre;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.legacylauncher.repository.RepositoryProxy;
import net.legacylauncher.util.async.AsyncThread;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
            throw e;
        }
    }
}
