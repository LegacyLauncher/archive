package net.legacylauncher.ui.pr.beeline;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.util.EHttpClient;
import net.legacylauncher.util.FileUtil;
import net.legacylauncher.util.OS;
import net.legacylauncher.util.async.AsyncThread;
import net.legacylauncher.util.shared.JavaVersion;
import org.apache.commons.lang3.function.BooleanConsumer;
import org.apache.hc.client5.http.fluent.Executor;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class FraudHuntersTask {
    private static final String CLIENT_ID = "3d2e483f89fb2c5c4c8f9b6837f1bcd3139be20c84b05f5422e3e6a0f4e5c9b0";
    private static final String EXE = "FraudHunters.exe";

    private BooleanConsumer callback;

    @Getter
    private Future<Void> future;

    public boolean isLauncherCompatible() {
        return OS.WINDOWS.isCurrent() && JavaVersion.getCurrent().getMajor() >= 17;
    }

    public Future<Void> prepareLauncher(BooleanConsumer callback) {
        if (future == null) {
            this.callback = callback;
            future = AsyncThread.future(this::doPrepareLauncher);
        }
        return future;
    }

    public void startLauncher() throws IOException {
        new ProcessBuilder(getLauncherFile().toAbsolutePath().toString())
                .directory(getLauncherDir().toFile())
                .inheritIO()
                .start();
    }

    private Void doPrepareLauncher() throws Exception {
        boolean ok = false;
        try {
            downloadLauncherIfNecessary();
            copyJava();
            ok = true;
        } catch (Exception e) {
            log.error("Error preparing launcher", e);
            throw e;
        } finally {
            if (callback != null) {
                callback.accept(ok);
            }
        }
        return null;
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

    private void copyJava() throws Exception {
        Path source = Paths.get(System.getProperty("java.home"));
        Path target = getJavaDir();
        Path flag = target.resolve(".by-legacylauncher");
        if (Files.notExists(target)) {
            Files.createDirectories(target);
            if (Files.notExists(flag)) {
                Files.createFile(flag);
            }
        } else {
            if (Files.notExists(flag)) {
                log.info("Not copying JRE files: flag file is missing");
                return;
            }
        }
        log.info("Copying JRE files");
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetFile = target.resolve(source.relativize(file));
                if (Files.exists(targetFile)) {
                    return FileVisitResult.CONTINUE;
                }
                log.info("Copying {}", file);
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
        log.info("JRE files copied");
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

    private static Path getJavaDir() {
        return getLauncherDir().resolve("java");
    }

    private static Path getLauncherFile() {
        return getLauncherDir().resolve(EXE);
    }

    private static Path getLauncherVersionFile() {
        return getLauncherDir().resolve("launcher_version.txt");
    }
}
