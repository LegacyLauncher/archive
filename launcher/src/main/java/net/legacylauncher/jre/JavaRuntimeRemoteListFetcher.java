package net.legacylauncher.jre;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.repository.RepositoryProxy;
import net.legacylauncher.util.async.AsyncThread;
import net.minecraft.launcher.versions.json.DateTypeAdapter;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
public class JavaRuntimeRemoteListFetcher {
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
            log.warn("Failed", e);
            throw e;
        }
    }
}
