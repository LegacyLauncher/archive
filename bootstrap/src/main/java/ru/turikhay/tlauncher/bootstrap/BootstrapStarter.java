package ru.turikhay.tlauncher.bootstrap;

import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.bootstrap.launcher.ProcessStarter;
import ru.turikhay.tlauncher.bootstrap.util.OS;
import ru.turikhay.tlauncher.bootstrap.util.U;
import ru.turikhay.util.JavaVersion;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class BootstrapStarter {
    public static void main(String[] args) throws Exception {
        int exitCode = start(args, false);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    static int start(String[] args, boolean waitForClose) throws Exception {
        Path currentDir = Paths.get(System.getProperty("tlauncher.bootstrap.dir", "."));

        List<String> jvmArgs = new ArrayList<>();
        jvmArgs.addAll(loadJvmArgs());
        jvmArgs.addAll(loadExternalArgs(currentDir, "bootargs"));

        List<String> appArgs = new ArrayList<>();
        Collections.addAll(appArgs, args);
        appArgs.addAll(loadExternalArgs(currentDir, "args"));

        Set<Path> classPath = new LinkedHashSet<>(ProcessStarter.getDefinedClasspath());
        classPath.add(getCurrentJar());

        Process process = ProcessStarter
                .startJarProcess(currentDir, classPath, Bootstrap.class.getName(), jvmArgs, appArgs)
                .inheritIO()
                .start();

        log("Inherit process started");

        if (!waitForClose) {
            return 0;
        }

        return process.waitFor();
    }

    private static List<String> loadJvmArgs() {
        List<String> jvmArgs = new ArrayList<>();
        jvmArgs.add("-Xmx128m");
        jvmArgs.add("-Dfile.encoding=UTF-8");
        jvmArgs.add("-Dtlauncher.systemCharset=" + Charset.defaultCharset().name());
        jvmArgs.add("-Dtlauncher.logFolder=" + OS.getSystemRelatedDirectory("tlauncher/logs", true));

        if (JavaVersion.getCurrent().getMajor() >= 9) {
            jvmArgs.add("--add-exports");
            jvmArgs.add("java.desktop/sun.awt=javafx.swing");
        }

        for (String propKey : System.getProperties().stringPropertyNames()) {
            if (propKey.startsWith("tlauncher.bootstrap.")) {
                String value = Objects.requireNonNull(System.getProperty(propKey), "property \"" + propKey + "\"");

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
        ArrayList<String> possibleNames = new ArrayList<>();
        addPossibleName(possibleNames, "-" + OS.CURRENT.nameLowerCase() + "-" + OS.Arch.CURRENT.nameLowerCase(), extension);
        addPossibleName(possibleNames, OS.CURRENT.nameLowerCase(), extension);
        addPossibleName(possibleNames, "", extension);
        return possibleNames;
    }

    private static List<String> loadExternalArgs(Path currentDir, String extension) {
        Path externalArgsFile = null;

        for (String possibleName : getPossibleExternalArgsFileNames(extension)) {
            Path file = currentDir.resolve(possibleName);
            if (Files.isRegularFile(file)) {
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

    private static List<String> loadArgsFromFile(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file);
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

    private static Path getCurrentJar() throws URISyntaxException {
        return Paths.get(BootstrapStarter.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    }

    private static void log(Object... o) {
        U.log("[BootstrapStarter]", o);
    }
}
