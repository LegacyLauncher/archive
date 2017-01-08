package ru.turikhay.tlauncher.bootstrap;

import ru.turikhay.tlauncher.bootstrap.launcher.ProcessStarter;
import ru.turikhay.tlauncher.bootstrap.util.U;
import ru.turikhay.tlauncher.bootstrap.util.stream.InputStreamCopier;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BootstrapStarter {
    public static void main(String[] args) throws Exception {
        start(args, false);
    }

    static void start(String[] args, boolean waitForClose) throws Exception {
        File currentDir = new File(".");

        List<String> jvmArgs = new ArrayList<String>();
        jvmArgs.add("-Xmx128m");
        jvmArgs.add("-Dfile.encoding=UTF-8");

        List<String> appArgs = new ArrayList<String>();
        Collections.addAll(appArgs, args);

        Process process = ProcessStarter.startJarProcess(currentDir, ProcessStarter.getSystemClasspath(), Bootstrap.class.getName(), jvmArgs, appArgs);

        log("Inherit process started");

        if(!waitForClose) {
            return;
        }

        InputStreamCopier
            input = new InputStreamCopier(process.getInputStream(), System.out),
            error = new InputStreamCopier(process.getErrorStream(), System.err);

        input.start();
        error.start();

        process.waitFor();

        input.interrupt();
        error.interrupt();
    }

    private static void log(Object... o) {
        U.log("[BootstrapStarter]", o);
    }
}
