package net.minecraft.launcher.process;

import ru.turikhay.util.FileUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcessMonitorThread extends Thread {
    private final JavaProcess process;

    ProcessMonitorThread(JavaProcess process) {
        this.process = process;
    }

    public void run() {
        Process raw = process.getRawProcess();

        BufferedReader buf = new BufferedReader(new InputStreamReader(raw.getInputStream(), FileUtil.getCharset()));
        String line;

        while (process.isRunning()) {
            try {
                for (; (line = buf.readLine()) != null; process.getSysOutLines().add(line)) {
                    if (process.getExitRunnable() != null) {
                        process.getExitRunnable().onJavaProcessLog(process, line);
                    }
                }
            } catch (IOException var15) {
            } finally {
                try {
                    buf.close();
                } catch (IOException var14) {
                    Logger.getLogger(ProcessMonitorThread.class.getName()).log(Level.SEVERE, null, var14);
                }
            }
        }

        if (process.getExitRunnable() != null) {
            process.getExitRunnable().onJavaProcessEnded(process);
        }

    }
}
