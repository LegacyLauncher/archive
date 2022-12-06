package net.minecraft.launcher.process;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class ProcessMonitor {
    private final Logger LOGGER = LogManager.getLogger(ProcessMonitor.class);

    private final JavaProcess process;
    private final Charset charset;

    private final StreamMonitorWait wait;

    ProcessMonitor(JavaProcess process, Charset charset) {
        this.process = process;
        this.charset = charset;

        Process rawProcess = process.getRawProcess();
        StreamMonitorThread
                sysOut = new StreamMonitorThread(rawProcess.getInputStream(), PrintStreamType.OUT),
                sysErr = new StreamMonitorThread(rawProcess.getErrorStream(), PrintStreamType.ERR);

        this.wait = new StreamMonitorWait(sysOut, sysErr);
    }

    public void start() {
        wait.start();
    }

    private class StreamMonitorWait extends Thread {
        private final StreamMonitorThread sysOut, sysErr;

        private StreamMonitorWait(StreamMonitorThread sysOut, StreamMonitorThread sysErr) {
            super(StreamMonitorWait.class.getSimpleName());
            this.sysOut = sysOut;
            this.sysErr = sysErr;
        }

        @Override
        public void run() {
            sysOut.start();
            sysErr.start();
            try {
                sysOut.join();
                sysErr.join();
            } catch (InterruptedException e) {
                LOGGER.warn("{} was interrupted. Will not send JavaProcessEnd event",
                        StreamMonitorWait.class.getSimpleName());
                return;
            }
            process.getHook().processDestroyed(process.getRawProcess());
            if (process.getExitRunnable() != null) {
                process.getExitRunnable().onJavaProcessEnded(process);
            }
        }
    }

    private class StreamMonitorThread extends Thread {
        private final InputStream inputStream;
        private final PrintStreamType streamType;

        public StreamMonitorThread(InputStream inputStream, PrintStreamType streamType) {
            super(StreamMonitorThread.class.getSimpleName() + "#" + streamType.name());
            this.inputStream = inputStream;
            this.streamType = streamType;
        }

        @Override
        public void run() {
            while (process.isRunning()) {
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream, charset));
                IOException suppressed = null;
                String line;
                try {
                    while ((line = r.readLine()) != null) {
                        JavaProcessListener listener = process.getExitRunnable();
                        if (listener == null) {
                            process.getSysOutLines().add(line);
                        } else {
                            listener.onJavaProcessPrint(process, streamType, line);
                        }
                    }
                } catch (IOException ioE) {
                    suppressed = ioE;
                } finally {
                    try {
                        r.close();
                    } catch (IOException ioE) {
                        if (suppressed != null) {
                            ioE.addSuppressed(suppressed);
                        }
                        LOGGER.warn("Error handling streams of {}", process, ioE);
                    }
                }
                try {
                    /*
                        Busy waiting is essential because we have no way of knowing if other process has
                        written anything to the buffer. Also, if we don't sleep here, and there's no buffered
                        lines (e.g STDERR almost always is empty), then we'll just burn CPU cycles.
                        On the other hand, we might lose some data if it's buffering too quickly (which is not normal)
                     */
                    //noinspection BusyWait
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    LOGGER.warn("Interrupted");
                    return;
                }
            }
        }
    }
}
