package ru.turikhay.tlauncher.configuration;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.managers.JavaManagerConfig;
import ru.turikhay.util.OS;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

public class ArgumentParser {
    private static final Map<String, Arg> argMap = new LinkedHashMap<>();

    static {
        add("help", "Prints help");
        add("debug", "Prints help");
        add("settings", "Specifies path to settings file").needsArg();
        addIgnored("no-gui");
        addIgnored("no-terminate");
        addConfig("directory", "minecraft.gamedir", "Specifies Minecraft directory");
        addIgnored("profiles");
        addConfig("java-executable", "minecraft.cmd", "Specifies Java executable");
        add("java-directory", "Deprecated. Specifies Java directory. Use java-executable instead").needsArg();
        addConfig("version", "login.version", "Specifies Minecraft version to run");
        add("username", "Specifies name of the user to select").needsArg();
        add("usertype", "Specifies type of the user to select (in case there are multiple of them)").needsArg();
        addConfig("javaargs", "minecraft.javaargs", "Specifies JVM arguments");
        addConfig("margs", "minecraft.args", "Specifies Minecraft arguments");
        addConfig("window", "minecraft.size", "Specifies window size in format: width;height");
        addConfig("background", "gui.background", "Specifies background image");
        addConfig("fullscreen", "minecraft.fullscreen", "Specifies whether fullscreen mode enabled or not");
        addConfig("theme", "gui.theme", "Specifies theme file");
        addConfig("jre-dir", JavaManagerConfig.PATH_ROOT_DIR, "Specifies where Mojang JRE is saved");
        addConfig("launch", "login.auto", "Specifies whether to launch Minecraft automatically", false);
        addIgnored("block-settings");
    }

    public static OptionSet parseArgs(String[] args) {
        OptionParser parser = createParser();
        return parser.parse(args);
    }

    public static String printHelp() {
        OptionParser parser = createParser();
        StringWriter writer = new StringWriter();
        try {
            parser.printHelpOn(writer);
        } catch (IOException ignored) {
        }
        writer.flush();
        return writer.toString();
    }

    public static ParsedConfigEntryMap extractConfigEntries(OptionSet set) {
        Map<String, ParsedConfigEntry> entries = new LinkedHashMap<>();
        if (set.has("java-directory") && !set.has("java-executable")) {
            ConfigAttachment cfa = (ConfigAttachment) argMap.get("java-executable").attachment;
            entries.put(cfa.path, new ParsedConfigEntry(
                    cfa,
                    set.valueOf("java-directory") + File.pathSeparator +
                            "bin" + File.pathSeparator +
                            "java" + (OS.WINDOWS.isCurrent() ? "w.exe" : "")
            ));
        }
        for (Arg arg : argMap.values()) {
            if (!(arg.attachment instanceof ConfigAttachment)) {
                continue;
            }
            ConfigAttachment cfa = (ConfigAttachment) arg.attachment;
            if (set.has(arg.name)) {
                Object value;
                if (arg.needsArg) {
                    value = set.valueOf(arg.name);
                } else {
                    value = Boolean.TRUE;
                }
                entries.put(cfa.path, new ParsedConfigEntry(cfa, String.valueOf(value)));
            }
        }
        return new ParsedConfigEntryMap(entries);
    }

    private static OptionParser createParser() {
        OptionParser parser = new OptionParser();
        for (Arg arg : argMap.values()) {
            OptionSpecBuilder b = parser.accepts(arg.name, arg.description);
            if (arg.needsArg) {
                b.withRequiredArg();
            }
        }
        return parser;
    }

    private static void addConfig(String name, @Nullable String path, String description, boolean needsArg) {
        Arg arg = new Arg(name, description, new ConfigAttachment(path, true));
        arg.needsArg = needsArg;
        argMap.put(name, arg);
    }

    private static void addConfig(String name, @Nullable String path, String description) {
        addConfig(name, path, description, true);
    }

    private static Arg add(String name, String description) {
        Arg arg = new Arg(name, description, null);
        argMap.put(name, arg);
        return arg;
    }

    private static void addIgnored(String name) {
        argMap.put(name, new Arg(name, "Deprecated & ignored", new IgnoredArgAttachment(name)));
    }

    private static class Arg {
        final String name;
        final String description;
        @Nullable final Attachment attachment;

        boolean needsArg;

        Arg(String name, String description, @Nullable Attachment attachment) {
            this.name = name;
            this.description = description;
            this.attachment = attachment;
        }

        Arg needsArg() {
            this.needsArg = true;
            return this;
        }
    }

    private interface Attachment {
    }

    private static class ConfigAttachment implements Attachment {
        final String path;
        final boolean constant;

        ConfigAttachment(String path, boolean constant) {
            this.path = path;
            this.constant = constant;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConfigAttachment that = (ConfigAttachment) o;
            return constant == that.constant && path.equals(that.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path, constant);
        }
    }

    private interface ActionAttachment extends Attachment {
        void execute();
    }

    private static class IgnoredArgAttachment implements ActionAttachment {
        private static final Logger LOGGER = LogManager.getLogger(IgnoredArgAttachment.class);

        final String name;

        public IgnoredArgAttachment(String name) {
            this.name = name;
        }

        @Override
        public void execute() {
            LOGGER.warn("Argument {} is ignored", name);
        }
    }

    public static class ParsedConfigEntry {
        private final ConfigAttachment attachment;
        private final @Nullable String value;

        private ParsedConfigEntry(ConfigAttachment attachment, @Nullable String value) {
            this.attachment = attachment;
            this.value = value;
        }

        public String getPath() {
            return attachment.path;
        }

        public boolean isConstant() {
            return attachment.constant;
        }

        @Nullable
        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParsedConfigEntry that = (ParsedConfigEntry) o;
            return attachment.equals(that.attachment) && Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(attachment, value);
        }

        @Override
        public String toString() {
            return "{key="+ attachment.path +",value="+ value +",constant="+ attachment.constant +"}";
        }
    }

    public static class ParsedConfigEntryMap {
        private final Map<String, ParsedConfigEntry> entryMap;
        private final Set<String> constants;

        ParsedConfigEntryMap(Map<String, ParsedConfigEntry> entryMap) {
            this.entryMap = entryMap;
            this.constants = entryMap.values().stream()
                    .filter(ParsedConfigEntry::isConstant)
                    .map(e -> e.attachment.path)
                    .collect(Collectors.toSet());
        }

        public Optional<ParsedConfigEntry> getByKey(String key) {
            return Optional.ofNullable(entryMap.get(key));
        }

        public Collection<ParsedConfigEntry> entries() {
            return entryMap.values();
        }

        public boolean contains(String key) {
            return entryMap.containsKey(key);
        }

        public Collection<String> constants() {
            return constants;
        }

        public boolean isConstant(String key) {
            return constants.contains(key);
        }

        @Override
        public String toString() {
            return "ParsedConfigEntryMap{" +
                    "entryMap=" + entryMap +
                    '}';
        }
    }
}
