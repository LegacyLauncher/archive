package net.minecraft.options;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OptionsFile {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern LINE_PATTERN = Pattern.compile("^(.+?):(.+)$");

    private final Map<String, String> options = new LinkedHashMap<>();
    private final File file;

    public OptionsFile(File file) {
        this.file = Objects.requireNonNull(file);
    }

    public File getFile() {
        return file;
    }

    public String get(String key) {
        return options.get(key);
    }

    public void set(String key, String value) {
        options.put(key, value);
    }

    public void clear() {
        options.clear();
    }

    public void fill(Map<String, String> map) {
        this.options.putAll(map);
    }

    public Map<String, String> copy() {
        return new LinkedHashMap<>(options);
    }

    public void read() throws IOException {
        clear();
        if (file.length() > 1024L * 1024L) {
            throw new IOException("reported file size is incredibly large: " + file.length());
        }
        try (FileInputStream in = new FileInputStream(file); Scanner scanner = new Scanner(in)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Matcher m = LINE_PATTERN.matcher(line);
                if (m.matches()) {
                    options.put(m.group(1), m.group(2));
                } else {
                    LOGGER.warn("Line skipped while parsing {}: {}", file, line);
                }
            }
        }
    }

    public void save() throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            for (Map.Entry<String, String> entry : options.entrySet()) {
                writer.append(entry.getKey()).append(':').append(entry.getValue()).append(System.lineSeparator());
            }
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("file", file.getAbsolutePath())
                .append("fileExist", file.isFile())
                .append("options", options)
                .build();
    }
}
