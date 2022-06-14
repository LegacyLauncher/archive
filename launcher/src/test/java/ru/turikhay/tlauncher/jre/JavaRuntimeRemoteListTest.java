package ru.turikhay.tlauncher.jre;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import org.apache.http.client.fluent.Request;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Date;

class JavaRuntimeRemoteListTest {

    @Test
    @Disabled
    void realTest() throws IOException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(JavaRuntimeRemoteList.class, new JavaRuntimeRemoteListDeserializer())
                .registerTypeAdapter(Date.class, new DateTypeAdapter(false))
                .create();
        String content = Request.Get(JavaRuntimeRemoteList.URL).execute().returnContent().toString();
        JavaRuntimeRemoteList remoteList = gson.fromJson(content, JavaRuntimeRemoteList.class);
        JavaRuntimeRemote runtime = remoteList.getCurrentPlatformFirstRuntimeCandidate("java-runtime-alpha").get();
        System.out.println(runtime.getName());
    }
}