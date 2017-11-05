package ru.turikhay.tlauncher.bootstrap;

import ru.turikhay.tlauncher.bootstrap.launcher.ProcessStarter;
import ru.turikhay.tlauncher.bootstrap.util.OS;
import ru.turikhay.tlauncher.bootstrap.util.U;
import ru.turikhay.tlauncher.bootstrap.util.stream.InputStreamCopier;
import shaded.org.apache.commons.io.IOUtils;
import shaded.org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public final class BootstrapStarter {
    public static void main(String[] args) throws Exception {
        int exitCode = start(args, false);
        if(exitCode != 0) {
            System.exit(exitCode);
        }
    }

    static int start(String[] args, boolean waitForClose) throws Exception {
        File currentDir = new File(".");
        log("Current dir: ", currentDir.getAbsolutePath());

        List<String> jvmArgs = new ArrayList<String>();
        jvmArgs.addAll(loadJvmArgs());
        jvmArgs.addAll(loadExternalArgs(currentDir, "bootargs"));

        List<String> appArgs = new ArrayList<String>();
        Collections.addAll(appArgs, args);
        appArgs.addAll(loadExternalArgs(currentDir, "args"));

        Set<File> classPath = new LinkedHashSet<File>();
        classPath.addAll(ProcessStarter.getDefinedClasspath());
        classPath.add(new File(BootstrapStarter.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));

        Process process = ProcessStarter.startJarProcess(currentDir, classPath, Bootstrap.class.getName(), jvmArgs, appArgs);

        log("Inherit process started");

        if (!waitForClose) {
            return 0;
        }

        InputStreamCopier
                input = new InputStreamCopier(process.getInputStream(), System.out),
                error = new InputStreamCopier(process.getErrorStream(), System.err);

        input.start();
        error.start();

        int exitCode = process.waitFor();

        input.interrupt();
        error.interrupt();

        return exitCode;
    }

    private static List<String> loadJvmArgs() {
        List<String> jvmArgs = new ArrayList<String>();
        jvmArgs.add("-Xmx128m");
        jvmArgs.add("-Dfile.encoding=UTF-8");

        for (String propKey : System.getProperties().stringPropertyNames()) {
            if (propKey.startsWith("tlauncher.bootstrap.")) {
                String value = U.requireNotNull(System.getProperty(propKey), "property \"" + propKey + "\"");

                String arg = "-D" + propKey + "=" + value;
                jvmArgs.add(arg);

                log("Transferring property: ", arg);
            }
        }
        return jvmArgs;
    }

    private static List<String> loadExternalArgs(File currentDir, String extension) {
        File externalArgsFile = new File(currentDir, "tlauncher-" + OS.CURRENT.nameLowerCase() + "-" + OS.Arch.CURRENT.nameLowerCase() + "." + extension);

        if (!externalArgsFile.isFile()) {
            externalArgsFile = new File(currentDir, "tlauncher-" + OS.CURRENT.nameLowerCase() + "." + extension);
        }

        if (!externalArgsFile.isFile()) {
            externalArgsFile = new File(currentDir, "tlauncher." + extension);
        }

        if (externalArgsFile.isFile()) {
            load:
            {

                log("Loading arguments from file:", externalArgsFile);

                final String content;
                FileInputStream in = null;
                try {
                    content = IOUtils.toString(in = new FileInputStream(externalArgsFile), U.UTF8);
                } catch (IOException ioE) {
                    log("Cannot load arguments from file:", externalArgsFile, ioE);
                    break load;
                } finally {
                    U.close(in);
                }

                return new ArrayList<String>() {
                    {
                        Collections.addAll(this, StringUtils.split(content, ' '));
                    }
                };
            }
        }

        return Collections.EMPTY_LIST;
    }

    private static void log(Object... o) {
        U.log("[BootstrapStarter]", o);
    }
}
