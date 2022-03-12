package ru.turikhay.tlauncher.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.CharsetData;
import ru.turikhay.util.FileUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

public class LogFile implements CharsetData {
    private static final Logger LOGGER = LogManager.getLogger(LogFile.class);

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
        return new BufferedInputStream(new FileInputStream(file));
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
        return new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), charset);
    }

    private void createIfNotExist() throws IOException {
        FileUtil.createFile(file);
    }

    private static long requestFileSize(File file) {
        BasicFileAttributes attributes;
        try {
            attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        } catch (IOException e) {
            LOGGER.warn("Couldn't read attributes of {}", file.getAbsolutePath(), e);
            return UNKNOWN_LENGTH;
        }
        return attributes.size();
    }

    public static LogFile usingUTF8(File file) {
        return new LogFile(file, StandardCharsets.UTF_8);
    }
}
