package net.minecraft.options;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.util.U;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class OptionsFile {
    private static final Pattern LINE_PATTERN = Pattern.compile("^(.+?):(.+)$");

    private final Map<String, String> options = new LinkedHashMap<>();
    private final File file;

    public OptionsFile(File file) {
        this.file = U.requireNotNull(file);
        logPrefix = "[OptionsFile:" + file.getAbsolutePath() + "]";
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
        try(FileInputStream in = new FileInputStream(file); Scanner scanner = new Scanner(in)) {
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Matcher m = LINE_PATTERN.matcher(line);
                if(m.matches()) {
                    options.put(m.group(1), m.group(2));
                } else {
                    log("line skipped:" + line);
                }
            }
        }
    }

    public void save() throws IOException {
        try(FileWriter writer = new FileWriter(file)) {
            for(Map.Entry<String, String> entry : options.entrySet()) {
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

    private final String logPrefix;
    private void log(Object... o) {
        U.log(logPrefix, o);
    }
}
