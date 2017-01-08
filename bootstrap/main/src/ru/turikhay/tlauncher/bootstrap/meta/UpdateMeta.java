package ru.turikhay.tlauncher.bootstrap.meta;

import ru.turikhay.tlauncher.bootstrap.Bootstrap;
import ru.turikhay.tlauncher.bootstrap.json.UpdateDeserializer;
import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.util.DataBuilder;
import shaded.com.google.gson.Gson;
import shaded.com.google.gson.JsonIOException;
import shaded.com.google.gson.annotations.Expose;
import ru.turikhay.tlauncher.bootstrap.exception.ExceptionList;
import ru.turikhay.tlauncher.bootstrap.json.Json;
import ru.turikhay.tlauncher.bootstrap.util.U;
import shaded.org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class UpdateMeta {
    private static final List<String> UPDATE_URL_LIST = Arrays.asList(
            "http://cdn.turikhay.ru/tlauncher/%s/bootstrap.json",
            "http://u.tlauncher.ru/%s/bootstrap.json",
            "http://repo.tlauncher.ru/%s/bootstrap.json",
            "http://turikhay.ru/tlauncher/%s/bootstrap.json",
            "http://tlaun.ch/%s/bootstrap.json"
    );

    public static Task<UpdateMeta> fetchFor(final String brand) throws ExceptionList {
        U.requireNotNull(brand,  "brand");

        return new Task<UpdateMeta>("update:" + brand) {
            @Override
            protected UpdateMeta execute() throws Exception {
                log("Requesting update for: " + brand);

                Gson gson = createGson();
                List<Exception> eList = new ArrayList<Exception>();
                for (int i = 0; i < UPDATE_URL_LIST.size(); i++) {
                    checkInterrupted();
                    updateProgress((i+1) / UPDATE_URL_LIST.size());

                    String _url = null;
                    long time = System.currentTimeMillis();

                    try {
                        _url = String.format(UPDATE_URL_LIST.get(i), brand);
                        URL url = new URL(_url);
                        log("URL: ", url);

                        UpdateMeta meta = fetchFrom(gson, url.openStream());
                        long delta = System.currentTimeMillis() - time;

                        updateProgress(1.);
                        log("Success!");
                        Bootstrap.recordBreadcrumb(UpdateMeta.class, "fetch_success", DataBuilder.create("url", url).add("delta_ms", delta));

                        return meta;
                    } catch (Exception e) {
                        Bootstrap.recordBreadcrumbError(UpdateMeta.class, "fetch_failed", e, DataBuilder.create("url", _url));
                        e.printStackTrace();
                        eList.add(e);
                    }
                }
                throw new ExceptionList(eList);
            }
        };
    }

    private static Gson createGson() {
        return Json.build().registerTypeAdapter(UpdateDeserializer.class, new UpdateDeserializer()).create();
    }

    private static UpdateMeta fetchFrom(Gson gson, InputStream in) throws Exception {
        String read = null;
        try {
            read = IOUtils.toString(U.toReader(in));
            return gson.fromJson(read, UpdateMeta.class);
        } catch(Exception e) {
            Throwable cause = e;

            if(e.getCause() != null && e.getCause() instanceof IOException) {
                if(read == null) {
                    throw (IOException) e.getCause();
                }
                cause = e.getCause();
            }

            if(read == null) {
                throw e;
            }

            if(read.length() > 100) {
                read = read.substring(0, 99) + "...";
            }

            throw new IOException("could not read: \""+ read +"\"", cause);
        }
    }

    public static UpdateMeta fetchFrom(InputStream in) throws Exception {
        return fetchFrom(createGson(), in);
    }

    protected UpdateEntry update;
    protected Map<String, String> support;
    @Expose
    protected String options; // this field is handled by UpdateDeserializer

    public RemoteBootstrapMeta getBootstrap() {
        return update == null? null : update.bootstrap;
    }

    public RemoteLauncherMeta getLauncher() {
        return update == null? null : update.launcher;
    }

    public Map<String, String> getSupport() {
        return support == null ? null : Collections.unmodifiableMap(support);
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    private static void log(Object... o) {
        U.log("[UpdateMeta]", o);
    }

    private static class UpdateEntry {
        private RemoteBootstrapMeta bootstrap;
        private RemoteLauncherMeta launcher;
    }
}
