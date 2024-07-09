package net.legacylauncher.logger;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.util.CharsetData;
import net.legacylauncher.util.FileUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

@Slf4j
public class LogFile implements CharsetData {
    private final File file;
    private final Charset charset;
    private long length = Long.MIN_VALUE;

    public LogFile(File file, Charset charset) {
        this.file = file;
        this.charset = charset;
    }

    public File getFile() {
        return file;
    }

    public Charset getCharset() {
        return charset;
    }

    @Override
    public InputStreamReader read() throws IOException {
        createIfNotExist();
        return new InputStreamReader(stream(), charset);
    }

    @Override
    public InputStream stream() throws IOException {
        createIfNotExist();
        return new BufferedInputStream(Files.newInputStream(file.toPath()));
    }

    @Override
    public Charset charset() {
        return getCharset();
    }

    @Override
    public long length() {
        if (length == Long.MIN_VALUE) {
            length = requestFileSize(file);
        }
        return length;
    }

    public OutputStreamWriter write() throws IOException {
        createIfNotExist();
        return new OutputStreamWriter(new BufferedOutputStream(Files.newOutputStream(file.toPath())), charset);
    }

    private void createIfNotExist() throws IOException {
        FileUtil.createFile(file);
    }

    private static long requestFileSize(File file) {
        BasicFileAttributes attributes;
        try {
            attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        } catch (IOException e) {
            log.warn("Couldn't read attributes of {}", file.getAbsolutePath(), e);
            return UNKNOWN_LENGTH;
        }
        return attributes.size();
    }

    public static LogFile usingUTF8(File file) {
        return new LogFile(file, StandardCharsets.UTF_8);
    }
}
