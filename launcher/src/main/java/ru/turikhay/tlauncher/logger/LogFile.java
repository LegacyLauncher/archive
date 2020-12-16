package ru.turikhay.tlauncher.logger;

import ru.turikhay.util.CharsetData;
import ru.turikhay.util.FileUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class LogFile implements CharsetData {
    private final File file;
    private final Charset charset;

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

    public InputStreamReader read() throws IOException {
        createIfNotExist();
        return new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), charset);
    }

    public OutputStreamWriter write()  throws IOException {
        createIfNotExist();
        return new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), charset);
    }

    private void createIfNotExist() throws IOException {
        FileUtil.createFile(file);
    }

    public static LogFile usingUTF8(File file) {
        return new LogFile(file, StandardCharsets.UTF_8);
    }
}
