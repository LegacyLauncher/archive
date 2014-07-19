package net.minecraft.launcher.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.turikhay.util.OS;

public class JavaProcessLauncher {
	private final String jvmPath;
	private final List<String> commands;
	private File directory;
	private ProcessBuilder process;

	public JavaProcessLauncher(String jvmPath, String[] commands) {
		if (jvmPath == null)
			jvmPath = OS.getJavaPath();

		this.jvmPath = jvmPath;
		this.commands = new ArrayList<String>();
		Collections.addAll(this.commands, commands);
	}

	public JavaProcess start() throws IOException {
		List<String> full = getFullCommands();
		return new JavaProcess(full, createProcess().start());
	}

	// If process is created, there won't be any change applied.
	public ProcessBuilder createProcess() {
		if (process == null)
			process = new ProcessBuilder(getFullCommands()).directory(
					this.directory).redirectErrorStream(true);
		return process;
	}

	List<String> getFullCommands() {
		List<String> result = new ArrayList<String>(this.commands);
		result.add(0, getJavaPath());
		return result;
	}

	public String getCommandsAsString() {
		List<String> parts = getFullCommands();
		StringBuilder full = new StringBuilder();
		boolean first = true;

		for (String part : parts) {
			if (first)
				first = false;
			else
				full.append(" ");
			full.append(part);
		}

		return full.toString();
	}

	public List<String> getCommands() {
		return this.commands;
	}

	public void addCommand(Object command) {
		this.commands.add(command.toString());
	}

	public void addCommand(Object key, Object value) {
		this.commands.add(key.toString());
		this.commands.add(value.toString());
	}

	public void addCommands(Object[] commands) {
		for (Object c : Arrays.asList(commands))
			this.commands.add(c.toString());
	}

	public void addSplitCommands(Object commands) {
		addCommands(commands.toString().split(" "));
	}

	public JavaProcessLauncher directory(File directory) {
		this.directory = directory;

		return this;
	}

	public File getDirectory() {
		return this.directory;
	}

	String getJavaPath() {
		return this.jvmPath;
	}

	@Override
	public String toString() {
		return "JavaProcessLauncher[commands=" + this.commands + ", java="
				+ this.jvmPath + "]";
	}
}