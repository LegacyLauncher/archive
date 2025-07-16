package net.legacylauncher.ui.pr.beeline;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.jre.JavaRuntimeLocal;
import net.legacylauncher.jre.JavaRuntimeLocalDiscoverer;
import net.legacylauncher.logger.Log4j2ContextHelper;
import net.legacylauncher.pasta.Pasta;
import net.legacylauncher.pasta.PastaFormat;
import net.legacylauncher.stats.Stats;
import net.legacylauncher.util.*;
import net.legacylauncher.util.async.AsyncThread;
import net.legacylauncher.util.shared.JavaVersion;
import net.minecraft.launcher.process.JavaProcess;
import net.minecraft.launcher.process.JavaProcessLauncher;
import net.minecraft.launcher.process.JavaProcessListener;
import net.minecraft.launcher.process.PrintStreamType;
import org.apache.commons.lang3.function.BooleanConsumer;
import org.apache.hc.client5.http.fluent.Executor;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
@Slf4j
public class FraudHuntersTask {
    private static final String CLIENT_ID = "3d2e483f89fb2c5c4c8f9b6837f1bcd3139be20c84b05f5422e3e6a0f4e5c9b0";
    private static final String COMPATIBLE_MOJANG_JRE = "java-runtime-delta";
    private static final String EXE = "FraudHunters.exe";

    @Nullable
    private final JavaRuntimeLocalDiscoverer javaRuntimeDiscoverer;

    private BooleanConsumer callback;

    @Getter
    private Future<Void> future;

    public boolean isLauncherCompatible() {
        return OS.WINDOWS.isCurrent() && hasCompatibleJava();
    }

    private boolean hasCompatibleJava() {
        return isCurrentJavaCompatible() || discoverCompatibleMojangJre().isPresent();
    }

    private boolean isCurrentJavaCompatible() {
        return JavaVersion.getCurrent().getMajor() >= 21;
    }

    private Optional<JavaRuntimeLocal> discoverCompatibleMojangJre() {
        if (javaRuntimeDiscoverer == null) {
            return Optional.empty();
        }
        return javaRuntimeDiscoverer.getCurrentPlatformRuntime(COMPATIBLE_MOJANG_JRE);
    }

    public Future<Void> prepareLauncher(BooleanConsumer callback) {
        if (future == null) {
            this.callback = callback;
            future = AsyncThread.future(this::doPrepareLauncher);
        }
        return future;
    }

    public void startLauncher() throws IOException {
        JavaProcess process = new JavaProcessLauncher(StandardCharsets.UTF_8, findLocalJre(), new String[]{"-jar", EXE})
                .directory(getLauncherDir().toFile())
                .start();
        Instant startedAt = Instant.now();
        AtomicBoolean ok = new AtomicBoolean(true);
        CountDownLatch latch = new CountDownLatch(1);
        process.safeSetExitRunnable(new JavaProcessListener() {
            @Override
            public void onJavaProcessPrint(JavaProcess process, PrintStreamType streamType, String line) {
                log.info("FraudHunters >> {}", line);
                if (line.contains("DPI Scale")) {
                    log.info("Found successful init flag");
                    latch.countDown();
                }
            }

            @Override
            public void onJavaProcessEnded(JavaProcess p) {
                log.info("FraudHunters launcher has closed: {}", p.getExitCode());
                if (p.getExitCode() != 0 || Duration.between(startedAt, Instant.now()).toMillis() < 1000) {
                    submitPrivateReport(Log4j2ContextHelper.getCurrentLogFile());
                    ok.set(false);
                }
                latch.countDown();
            }

            @Override
            public void onJavaProcessError(JavaProcess p, Throwable e) {
                log.error("FraudHunters launcher error", e);
                submitPrivateReport(e);
                ok.set(false);
                latch.countDown();
            }
        });
        boolean countedDown;
        try {
            countedDown = latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("Interrupted", e);
            return;
        }
        if (!countedDown) {
            log.warn("FraudHunters launcher is starting for too long. Gave up waiting, assuming it's ok");
            return;
        }
        if (!ok.get()) {
            log.warn("FraudHunters launcher has failed to start");
            throw new RuntimeException("Something went wrong");
        }
        log.info("FraudHunters launcher has started normally");
    }

    private String findLocalJre() {
        if (isCurrentJavaCompatible()) {
            log.info("Using current JRE");
            return OS.getJavaPath();
        }
        log.info("Using Mojang JRE");
        JavaRuntimeLocal localJre = discoverCompatibleMojangJre().orElseThrow(() -> new RuntimeException("Compatible Mojang JRE was not discovered"));
        return localJre.getExecutableFile().getAbsolutePath();
    }

    private Void doPrepareLauncher() throws Exception {
        boolean ok = false;
        try {
            downloadLauncherIfNecessary();
            ok = true;
        } catch (Exception e) {
            log.error("Error preparing launcher", e);
            submitPrivateReport(e);
            throw e;
        } finally {
            if (callback != null) {
                callback.accept(ok);
            }
        }
        return null;
    }

    private static void submitPrivateReport(CharsetData data) {
        AsyncThread.execute(() -> {
            String pastaUrl = Pasta.paste(data, PastaFormat.PLAIN);
            Stats.fraudHuntersReport(pastaUrl);
        });
    }

    private static void submitPrivateReport(Throwable t) {
        submitPrivateReport(new StringCharsetData(U.printStackTrace(t)));
    }

    private void downloadLauncherIfNecessary() throws Exception {
        CompletableFuture<String> remoteVersion = queryLauncherVersion();
        if (checkIfLauncherIntact()) {
            return;
        }
        log.info("Preparing to download the launcher");
        Path launcherDir = getLauncherDir();
        if (!Files.exists(launcherDir)) {
            Files.createDirectories(launcherDir);
        }
        Path launcherFile = getLauncherFile();
        Request dl = prepareLauncherApiRequest("/file/" + remoteVersion.get());
        AtomicBoolean ok = new AtomicBoolean(false);
        try (CloseableHttpClient client = EHttpClient.createRepeatable(); OutputStream output = Files.newOutputStream(launcherFile)) {
            Executor executor = Executor.newInstance(client);
            executor.execute(dl).handleResponse(response -> {
               if (response.getCode() != 200) {
                   throw new IOException("Failed to download launcher: " + response.getCode());
               }
               log.info("Downloading launcher...");
               response.getEntity().writeTo(output);
               log.info("Launcher downloaded");
               ok.set(true);
               return null;
            });
        } finally {
            if (!ok.get()) {
                Files.deleteIfExists(launcherFile);
            }
        }
        log.info("Writing launcher version file");
        Files.write(getLauncherVersionFile(), remoteVersion.get().getBytes(StandardCharsets.UTF_8));
    }

    private boolean checkIfLauncherIntact() throws Exception {
        Path launcherFile = getLauncherFile();
        if (Files.exists(launcherFile)) {
            CompletableFuture<String> remoteHash = queryLauncherHash();
            String localHash = getLocalLauncherChecksum();
            if (localHash.equals(remoteHash.get())) {
                log.info("Local launcher is intact. No need to download anything");
                return true;
            }
            log.info("Local launcher differs from the one on the server");
        } else {
            log.info("Local launcher is missing");
        }
        return false;
    }

    CompletableFuture<String> queryLauncherVersion() {
        return queryLauncherApi("/version", "version");
    }

    CompletableFuture<String> queryLauncherHash() {
        return queryLauncherApi("/hash", "hash");
    }

    private Request prepareLauncherApiRequest(String endpoint) {
        return Request.get("https://proxy.scamcity.ru/launcher" + endpoint)
                .addHeader("Client-ID", CLIENT_ID);
    }

    private CompletableFuture<String> queryLauncherApi(String endpoint, String field) {
        return AsyncThread.completableFuture(() -> {
            log.info("Querying {}", endpoint);
            String json = EHttpClient.toString(prepareLauncherApiRequest(endpoint));
            log.info("Endpoint {} returned: {}", endpoint, json);
            JsonObject object = JsonParser.parseString(json).getAsJsonObject();
            return object.get(field).getAsString();
        });
    }

    private String getLocalLauncherChecksum() throws IOException {
        String checksum = FileUtil.getChecksum0(getLauncherFile().toFile(), "SHA-256");
        log.info("Local checksum: {}", checksum);
        return checksum;
    }

    private static Path getLauncherDir() {
        if (!OS.WINDOWS.isCurrent()) {
            throw new RuntimeException("os not supported");
        }
        return Paths.get(Objects.requireNonNull(System.getenv("LOCALAPPDATA"), "missing LOCALAPPDATA")).resolve("FraudHunters");
    }

    private static Path getLauncherFile() {
        return getLauncherDir().resolve(EXE);
    }

    private static Path getLauncherVersionFile() {
        return getLauncherDir().resolve("launcher_version.txt");
    }
}
