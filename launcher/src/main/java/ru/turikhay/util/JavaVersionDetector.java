package ru.turikhay.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.async.AsyncThread;
import ru.turikhay.util.jvd.JavaVersionDetectorMain;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaVersionDetector {
    private static final Pattern PATTERN = Pattern.compile("^\\$#! java: (.+)$");

    private static final Logger LOGGER = LogManager.getLogger(JavaVersionDetector.class);


    private final String javaExec;

    public JavaVersionDetector(String javaExec) {
        this.javaExec = javaExec;
    }

    public JavaVersion detect() throws JavaVersionNotDetectedException, InterruptedException {
        Future<JavaVersion> task = AsyncThread.future(this::doDetect);
        try {
            return task.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof JavaVersionNotDetectedException) {
                throw (JavaVersionNotDetectedException) e.getCause();
            } else {
                throw new JavaVersionNotDetectedException(e);
            }
        } catch (TimeoutException e) {
            throw new JavaVersionNotDetectedException(e);
        } finally {
            task.cancel(true);
        }
    }

    private JavaVersion doDetect() throws JavaVersionNotDetectedException, InterruptedException {
        File file;
        try {
            file = new File(JavaVersionDetectorMain.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new JavaVersionNotDetectedException(e);
        }
        List<String> command = Arrays.asList(
                javaExec,
                "-cp",
                file.getAbsolutePath(),
                JavaVersionDetectorMain.class.getName()
        );
        ProcessBuilder b = new ProcessBuilder(command);
        b.redirectErrorStream(true);

        Process process;
        try {
            process = b.start();
        } catch (IOException e) {
            throw new JavaVersionNotDetectedException(e);
        }
        LOGGER.debug("Started process: {}", command);

        try (BufferedReader input = new BufferedReader(new InputStreamReader(
                process.getInputStream(),
                StandardCharsets.US_ASCII
        ))) {
            String line;
            while ((line = input.readLine()) != null) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                LOGGER.debug("[{}] Line: {}", javaExec, line);
                Matcher matcher = PATTERN.matcher(line);
                if (matcher.matches()) {
                    LOGGER.debug("[{}] Found matching line: {}", javaExec, line);
                    try {
                        return JavaVersion.parse(matcher.group(1));
                    } catch (RuntimeException e) {
                        LOGGER.warn("[{}] Couldn't parse version line: {}", javaExec, line);
                        throw new JavaVersionNotDetectedException(line);
                    }
                }
            }
        } catch (IOException e) {
            throw new JavaVersionNotDetectedException(e);
        } finally {
            process.destroy();
        }
        int exitCode;
        try {
            exitCode = process.exitValue();
        } catch (IllegalThreadStateException e) {
            throw new JavaVersionNotDetectedException("output closed");
        }
        if (exitCode == 0) {
            throw new JavaVersionNotDetectedException("no result");
        } else {
            throw new JavaVersionNotDetectedException(String.format(Locale.ROOT,
                    "exit code: 0x%08X (%d)", exitCode, exitCode
            ));
        }
    }

}
