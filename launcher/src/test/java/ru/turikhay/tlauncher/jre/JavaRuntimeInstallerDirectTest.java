package ru.turikhay.tlauncher.jre;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import org.apache.http.client.fluent.Request;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.Date;

class JavaRuntimeInstallerDirectTest {

    @Test
    @Disabled
    void realTest() throws IOException, InterruptedException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(JavaRuntimeRemoteList.class, new JavaRuntimeRemoteListDeserializer())
                .registerTypeAdapter(Date.class, new DateTypeAdapter(false))
                .create();
        String content = Request.Get(JavaRuntimeRemoteList.URL).execute().returnContent().toString();
        JavaRuntimeRemoteList remoteList = gson.fromJson(content, JavaRuntimeRemoteList.class);
        JavaRuntimeRemote runtime = remoteList.getCurrentPlatformFirstRuntimeCandidate("java-runtime-alpha").get();
        JavaRuntimeInstallerDirect installer = new JavaRuntimeInstallerDirect(
                new File("D:\\runtime"),
                runtime
        );
        installer.install(Mockito.mock(ProgressReporter.class));
    }

}