package net.minecraft.launcher.process;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.minecraft.launcher.ProcessHook;
import net.legacylauncher.util.OS;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class JavaProcessLauncher {
    public static final String COMMAND_TOKEN = "%command%";
    private final Charset charset;
    private final String jvmPath;
    private final List<String> commands;
    private final boolean allowArgumentFiles;
    private File directory;
    private ProcessBuilder process;
    private ProcessHook hook = ProcessHook.None.INSTANCE;
    private List<String> wrapperCommand;
    private static final Charset nativeCharset = getNativeCharset();

    public JavaProcessLauncher(Charset charset, String jvmPath, boolean allowArgumentFiles) {
        if (jvmPath == null) {
            jvmPath = OS.getJavaPath();
        }

        this.charset = charset;
        this.jvmPath = jvmPath;
        this.allowArgumentFiles = allowArgumentFiles;
        this.commands = new ArrayList<>();
    }

    public String getJvmPath() {
        return jvmPath;
    }

    public JavaProcess start() throws IOException {
        return new JavaProcess(createProcess().start(), charset, hook);
    }

    public ProcessBuilder createProcess() {
        if (process == null) {
            List<String> fullCommand;
            if (allowArgumentFiles) {
                try {
                    Path argsFile = Files.createTempFile("ll", "args");
                    log.info("Using @argfile, path: {}, charset: {}", argsFile, nativeCharset);
                    try (Writer writer = Files.newBufferedWriter(argsFile, nativeCharset)) {
                        for (String arg : commands) {
                            writer.append(escapeArgFile(arg));
                            writer.append("\n");
                        }
                    }
                    fullCommand = wrapCommand(Stream.of(getJavaPath(), String.format("@%s", argsFile.toAbsolutePath())));
                    addHook(new DeleteFileHook(argsFile));
                } catch (Exception ignored) {
                    // fallback to plain args command
                    fullCommand = getFullCommands();
                }
            } else {
                fullCommand = getFullCommands();
            }
            process = new ProcessBuilder(fullCommand);
            process.directory(directory).redirectErrorStream(true);
            hook.enrichProcess(process);
        }

        return process;
    }

    private String escapeArgFile(String arg) {
        if ("".equals(arg)) {
            return "\"\"";
        }
        if (!arg.contains(" ") && !arg.contains("\n") && !arg.contains("\r") && !arg.contains("\t") && !arg.contains("\f") && !arg.contains("\\")) {
            return arg;
        }
        StringBuilder escaped = new StringBuilder();
        escaped.append("\"");
        for (int i = 0; i < arg.length(); i++) {
            char c = arg.charAt(i);
            switch (c) {
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                default:
                    escaped.append(c);
                    break;
            }
        }
        escaped.append("\"");
        return escaped.toString();
    }

    private List<String> getFullCommands() {
        return wrapCommand(Stream.concat(Stream.of(getJavaPath()), commands.stream()));
    }

    private List<String> wrapCommand(Stream<? extends String> command) {
        Stream<String> cmdLine;
        if (wrapperCommand == null || wrapperCommand.isEmpty()) {
            cmdLine = Stream.of(COMMAND_TOKEN);
        } else {
            cmdLine = wrapperCommand.stream();
        }
        return cmdLine.flatMap(s -> {
            if (COMMAND_TOKEN.equals(s)) {
                return command;
            } else {
                return Stream.of(s);
            }
        }).collect(Collectors.toList());
    }

    public String getCommandsAsString() {
        List<String> parts = getFullCommands();
        StringBuilder full = new StringBuilder();

        for (String part : parts) {
            if (full.length() != 0) {
                full.append(' ');
            }
            full.append(part);
        }

        return full.toString();
    }

    public List<String> getCommands() {
        return commands;
    }

    public void addCommand(Object command) {
        commands.add(command.toString());
    }

    public void addCommand(Object key, Object value) {
        commands.add(key.toString());
        commands.add(value.toString());
    }

    public void addCommands(Object[] commands) {

        for (Object c : commands) {
            this.commands.add(c.toString());
        }

    }

    public void addSplitCommands(Object commands) {
        addCommands(commands.toString().split(" "));
    }

    public ProcessHook getHook() {
        return hook;
    }

    public void addHook(ProcessHook hook) {
        if (this.process != null) {
            throw new IllegalStateException("Could not add hook after process being created");
        }
        this.hook = this.hook.then(hook);
    }

    public JavaProcessLauncher directory(File directory) {
        this.directory = directory;
        return this;
    }

    public File getDirectory() {
        return directory;
    }

    String getJavaPath() {
        return jvmPath;
    }

    public String toString() {
        return "JavaProcessLauncher[commands=" + commands + ", java=" + jvmPath + "]";
    }

    public void wrapperCommand(List<String> wrapperCommand) {
        if (wrapperCommand.stream().filter(COMMAND_TOKEN::equals).count() != 1) {
            throw new IllegalStateException("%command% token should be presented exactly once");
        }
        this.wrapperCommand = wrapperCommand;
    }

    private static Charset getNativeCharset() {
        String charset = System.getProperty("native.encoding", System.getProperty("sun.jnu.encoding"));
        if (charset != null)
            try {
                return Charset.forName(charset);
            } catch (UnsupportedCharsetException ignored) {
                return Charset.defaultCharset();
            }
        return Charset.defaultCharset();
    }

    private static class DeleteFileHook implements ProcessHook {
        private final Path path;

        private DeleteFileHook(Path path) {
            this.path = path;
        }

        @Override
        public void processDestroyed(Process process) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
            }
        }
    }
}
