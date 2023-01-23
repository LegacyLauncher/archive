package net.minecraft.launcher.process;

import ru.turikhay.tlauncher.minecraft.launcher.ProcessHook;

import java.nio.charset.Charset;

public class JavaProcess {
    private static final int MAX_SYSOUT_LINES = 5;

    private final LimitedCapacityList<String> sysOutLines = new LimitedCapacityList<>(String.class, MAX_SYSOUT_LINES);
    private JavaProcessListener listener;
    private final Process process;
    private final ProcessMonitor monitor;
    private final Charset charset;
    private final ProcessHook hook;

    public JavaProcess(Process process, Charset charset, ProcessHook hook) {
        this.process = process;
        this.charset = charset;
        this.hook = hook;
        monitor = new ProcessMonitor(this, charset);
        monitor.start();
        hook.processCreated(process);
    }

    public Charset getCharset() {
        return charset;
    }

    public ProcessMonitor getMonitor() {
        return monitor;
    }

    public ProcessHook getHook() {
        return hook;
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
        return process.isAlive();
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
