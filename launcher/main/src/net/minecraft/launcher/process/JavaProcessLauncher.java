package net.minecraft.launcher.process;

import ru.turikhay.util.OS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class JavaProcessLauncher {
    private final String jvmPath;
    private final List<String> commands;
    private File directory;
    private ProcessBuilder process;

    public JavaProcessLauncher(String jvmPath, String[] commands) {
        if (jvmPath == null) {
            jvmPath = OS.getJavaPath();
        }

        this.jvmPath = jvmPath;
        this.commands = new ArrayList<String>();
        Collections.addAll(this.commands, commands);
    }

    public String getJvmPath() {
        return jvmPath;
    }

    public JavaProcess start() throws IOException {
        return new JavaProcess(createProcess().start());
    }

    public ProcessBuilder createProcess() {
        if (process == null) {
            process = (new ProcessBuilder(getFullCommands())).directory(directory).redirectErrorStream(true);
        }

        return process;
    }

    List<String> getFullCommands() {
        ArrayList result = new ArrayList(commands);
        result.add(0, getJavaPath());
        return result;
    }

    public String getCommandsAsString() {
        List parts = getFullCommands();
        StringBuilder full = new StringBuilder();
        boolean first = true;

        String part;
        for (Iterator var5 = parts.iterator(); var5.hasNext(); full.append(part)) {
            part = (String) var5.next();
            if (first) {
                first = false;
            } else {
                full.append(' ');
            }
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
        Object[] var5 = commands;
        int var4 = commands.length;

        for (int var3 = 0; var3 < var4; ++var3) {
            Object c = var5[var3];
            this.commands.add(c.toString());
        }

    }

    public void addSplitCommands(Object commands) {
        addCommands(commands.toString().split(" "));
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
