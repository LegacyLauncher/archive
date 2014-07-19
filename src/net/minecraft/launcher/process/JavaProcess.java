package net.minecraft.launcher.process;

import java.util.List;

public class JavaProcess {
	private static final int MAX_SYSOUT_LINES = 5;
	private final List<String> commands;
	private final Process process;
	private final LimitedCapacityList<String> sysOutLines = new LimitedCapacityList<String>(
			String.class, MAX_SYSOUT_LINES);
	private JavaProcessListener onExit;

	public JavaProcess(List<String> commands, Process process) {
		this.commands = commands;
		this.process = process;

		ProcessMonitorThread monitor = new ProcessMonitorThread(this);
		monitor.start();
	}

	public Process getRawProcess() {
		return this.process;
	}

	public List<String> getStartupCommands() {
		return this.commands;
	}

	public String getStartupCommand() {
		return this.process.toString();
	}

	public LimitedCapacityList<String> getSysOutLines() {
		return this.sysOutLines;
	}

	public boolean isRunning() {
		try {
			this.process.exitValue();
		} catch (IllegalThreadStateException ex) {
			return true;
		}

		return false;
	}

	public void setExitRunnable(JavaProcessListener runnable) {
		this.onExit = runnable;
	}

	public void safeSetExitRunnable(JavaProcessListener runnable) {
		setExitRunnable(runnable);

		if ((!isRunning()) && (runnable != null))
			runnable.onJavaProcessEnded(this);
	}

	public JavaProcessListener getExitRunnable() {
		return this.onExit;
	}

	public int getExitCode() {
		try {
			return this.process.exitValue();
		} catch (IllegalThreadStateException ex) {
			ex.fillInStackTrace();
			throw ex;
		}
	}

	@Override
	public String toString() {
		return "JavaProcess[commands=" + this.commands + ", isRunning="
				+ isRunning() + "]";
	}

	public void stop() {
		this.process.destroy();
	}
}