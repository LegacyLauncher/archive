package ru.turikhay.tlauncher.jre;

import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.FileUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class JavaRuntimeLocalIntegrityInfo {
    private static final Logger LOGGER = LogManager.getLogger(JavaRuntimeLocalIntegrityInfo.class);

    private final List<FileIntegrityEntry> entries;

    public JavaRuntimeLocalIntegrityInfo(List<FileIntegrityEntry> entries) {
        this.entries = Objects.requireNonNull(entries);
    }

    public List<FileIntegrityEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public List<FileIntegrityEntry> checkEntriesResolving(File directory) {
        return this.entries.stream().filter(e -> e.isTamperedWithAt(directory)).collect(Collectors.toList());
    }

    public void write(File file) throws IOException {
        try(OutputStreamWriter writer = new OutputStreamWriter(
                new BufferedOutputStream(new FileOutputStream(file)), StandardCharsets.UTF_8)) {
            for (FileIntegrityEntry entry : entries) {
                writer.write(entry.toString());
                writer.write(' ');
            }
        }
    }

    public static JavaRuntimeLocalIntegrityInfo parse(File file) throws IOException {
        ArrayList<FileIntegrityEntry> entries = new ArrayList<>();
        try(InputStream input = new FileInputStream(file)) {
            Scanner scanner = new Scanner(input, StandardCharsets.UTF_8.name());
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] f1 = StringUtils.splitByWholeSeparator(line, " /#// ", 2);
                String path = f1[0];
                String[] f2 = StringUtils.splitByWholeSeparator(f1[1], " ", 2);
                String sha1 = f2[0];
                long lastModified = Long.parseLong(f2[1]);
                entries.add(new FileIntegrityEntry(path, sha1, lastModified));
            }
        }
        return new JavaRuntimeLocalIntegrityInfo(entries);
    }

    public static JavaRuntimeLocalIntegrityInfo generate(JavaRuntimeManifest manifest, File workingDirectory)
            throws IOException {
        List<FileIntegrityEntry> entries = new ArrayList<>();
        for (JavaRuntimeManifest.RuntimeFile runtimeFile : manifest.getFiles()) {
            if(!runtimeFile.isFile()) {
                continue;
            }
            File realFile = new File(workingDirectory, runtimeFile.getPath());
            if(!realFile.isFile()) {
                throw new FileNotFoundException(runtimeFile.getPath());
            }
            String sha1;
            if(runtimeFile.getDownload() != null) {
                sha1 = runtimeFile.getDownload().getSha1();
            } else {
                LOGGER.warn("Manifest file {} doesn't contain raw download. Generating SHA-1",
                        runtimeFile.getPath());
                Sentry.capture(new EventBuilder()
                        .withLevel(Event.Level.WARNING)
                        .withMessage("manifest file doesn't contain raw download")
                        .withExtra("manifestFile", runtimeFile)
                );
                sha1 = FileUtil.getSha1(realFile);
            }
            entries.add(new FileIntegrityEntry(
                    runtimeFile.getPath(),
                    sha1,
                    Files.getLastModifiedTime(realFile.toPath()).to(TimeUnit.NANOSECONDS)
            ));
        }
        return new JavaRuntimeLocalIntegrityInfo(entries);
    }
}
