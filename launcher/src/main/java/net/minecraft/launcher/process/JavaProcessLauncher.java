package net.minecraft.launcher.process;

import ru.turikhay.tlauncher.minecraft.launcher.ProcessHook;
import ru.turikhay.util.OS;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JavaProcessLauncher {
    private final Charset charset;
    private final String jvmPath;
    private final List<String> commands;
    private File directory;
    private ProcessBuilder process;
    private ProcessHook hook = ProcessHook.None.INSTANCE;

    public JavaProcessLauncher(Charset charset, String jvmPath, String[] commands) {
        if (jvmPath == null) {
            jvmPath = OS.getJavaPath();
        }

        this.charset = charset;
        this.jvmPath = jvmPath;
        this.commands = new ArrayList<>();
        Collections.addAll(this.commands, commands);
    }

    public String getJvmPath() {
        return jvmPath;
    }

    public JavaProcess start() throws IOException {
        return new JavaProcess(createProcess().start(), charset, hook);
    }

    public ProcessBuilder createProcess() {
        if (process == null) {
            process = (new ProcessBuilder(getFullCommands())).directory(directory).redirectErrorStream(true);
            hook.enrichProcess(process);
        }

        return process;
    }

    List<String> getFullCommands() {
        List<String> result = new ArrayList<>(commands.size() + 1);
        result.add(getJavaPath());
        result.addAll(commands);
        return result;
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
}
