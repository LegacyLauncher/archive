package net.minecraft.launcher.process;

public class JavaProcess {
    private static final int MAX_SYSOUT_LINES = 5;

    private final LimitedCapacityList<String> sysOutLines = new LimitedCapacityList<String>(String.class, MAX_SYSOUT_LINES);
    private JavaProcessListener listener;
    private final Process process;
    private final ProcessMonitorThread monitor;

    public JavaProcess(Process process) {
        this.process = process;
        monitor = new ProcessMonitorThread(this);
        monitor.start();
    }

    public ProcessMonitorThread getMonitor() {
        return monitor;
    }

    public Process getRawProcess() {
        return process;
    }

    public String getStartupCommand() {
        return process.toString();
    }

    public LimitedCapacityList<String> getSysOutLines() {
        return sysOutLines;
    }

    public boolean isRunning() {
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException var2) {
            return true;
        }
    }

    public void setExitRunnable(JavaProcessListener runnable) {
        listener = runnable;
    }

    public void safeSetExitRunnable(JavaProcessListener runnable) {
        setExitRunnable(runnable);
        if (!isRunning() && runnable != null) {
            runnable.onJavaProcessEnded(this);
        }

    }

    public JavaProcessListener getExitRunnable() {
        return listener;
    }

    public int getExitCode() {
        try {
            return process.exitValue();
        } catch (IllegalThreadStateException var2) {
            var2.fillInStackTrace();
            throw var2;
        }
    }

    public String toString() {
        return "JavaProcess[process=" + process + ", isRunning=" + isRunning() + "]";
    }

    public void stop() {
        process.destroy();
    }
}
