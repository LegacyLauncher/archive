package net.minecraft.launcher.process;

import ru.turikhay.tlauncher.portals.Portals;

import java.nio.charset.Charset;

public class JavaProcess {
    private static final int MAX_SYSOUT_LINES = 5;

    private final LimitedCapacityList<String> sysOutLines = new LimitedCapacityList<>(String.class, MAX_SYSOUT_LINES);
    private JavaProcessListener listener;
    private final Process process;
    private final ProcessMonitor monitor;
    private final Charset charset;

    public JavaProcess(Process process, Charset charset) {
        this.process = process;
        this.charset = charset;
        monitor = new ProcessMonitor(this, charset);
        monitor.start();
        Portals.getPortal().minecraftProcessCreated(process);
    }

    public Charset getCharset() {
        return charset;
    }

    public ProcessMonitor getMonitor() {
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
