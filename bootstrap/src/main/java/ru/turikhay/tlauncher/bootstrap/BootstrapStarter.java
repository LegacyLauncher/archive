package ru.turikhay.tlauncher.bootstrap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.bootstrap.launcher.ProcessStarter;
import ru.turikhay.tlauncher.bootstrap.util.OS;
import ru.turikhay.tlauncher.bootstrap.util.U;
import ru.turikhay.tlauncher.bootstrap.util.stream.InputStreamCopier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
        jvmArgs.add("-Dtlauncher.systemCharset=" + Charset.defaultCharset().name());
        jvmArgs.add("-Dtlauncher.logFolder=" + OS.getSystemRelatedDirectory("tlauncher/logs", true));

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

    private static void addPossibleName(ArrayList<String> possibleNames, String name, String extension) {
        possibleNames.add("tl" + name + "." + extension);
        possibleNames.add("tlauncher" + name + "." + extension);
    }

    private static List<String> getPossibleExternalArgsFileNames(String extension) {
        ArrayList<String> possibleNames = new ArrayList<String>();
        addPossibleName(possibleNames, "-" + OS.CURRENT.nameLowerCase() + "-" + OS.Arch.CURRENT.nameLowerCase(), extension);
        addPossibleName(possibleNames, OS.CURRENT.nameLowerCase(), extension);
        addPossibleName(possibleNames, "", extension);
        return possibleNames;
    }

    private static List<String> loadExternalArgs(File currentDir, String extension) {
        File externalArgsFile = null;

        for(String possibleName : getPossibleExternalArgsFileNames(extension)) {
            File file = new File(currentDir, possibleName);
            if(file.isFile()) {
                externalArgsFile = file;
                break;
            }
        }

        if (externalArgsFile != null) {
            log("Loading arguments from file:", externalArgsFile);
            try {
                return loadArgsFromFile(externalArgsFile);
            } catch (IOException ioE) {
                log("Cannot load arguments from file:", externalArgsFile, ioE);
            }
        }
        return Collections.emptyList();
    }

    private static List<String> loadArgsFromFile(File file) throws IOException {
        List<String> lines;
        try(InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            lines = IOUtils.readLines(reader);
        }
        // remove all blank lines
        lines.removeIf(StringUtils::isBlank);
        switch (lines.size()) {
            case 0:
                // ???
                throw new IOException("no lines found");
            case 1:
                // only one line: old args file format
                return Arrays.asList(StringUtils.split(lines.get(0), ' '));
            default:
                // 2+ lines: new args file format

                // remove all comments
                lines.removeIf(line -> line.startsWith("#"));

                return lines;
        }
    }

    private static void log(Object... o) {
        U.log("[BootstrapStarter]", o);
    }

    private static class ShutdownHook extends Thread {
        ShutdownHook() {

        }
    }
}
