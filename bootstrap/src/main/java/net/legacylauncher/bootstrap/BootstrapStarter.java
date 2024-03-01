package net.legacylauncher.bootstrap;

import net.legacylauncher.bootstrap.util.OS;
import net.legacylauncher.util.shared.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public final class BootstrapStarter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapStarter.class);

    public static void main(String[] args) throws Exception {
        start(args, false);
    }

    static void start(String[] args, boolean debug) throws Exception {
        Path currentDir = Paths.get(System.getProperty("tlauncher.bootstrap.dir", "."));

        List<String> jvmArgs = new ArrayList<>();
        jvmArgs.addAll(loadJvmArgs());
        jvmArgs.addAll(loadExternalArgs(currentDir, "bootargs"));

        List<String> appArgs = new ArrayList<>();
        appArgs.add(Bootstrap.class.getName());
        Collections.addAll(appArgs, args);
        appArgs.addAll(loadExternalArgs(currentDir, "args"));

        Set<Path> classPath = new LinkedHashSet<>(BoostrapRestarter.getDefinedClasspath());
        classPath.add(getCurrentJar());

        BoostrapRestarter starter = BoostrapRestarter.create();
        int exitCode = starter.start(currentDir, jvmArgs, classPath, appArgs, debug);
        System.exit(exitCode);
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
            jvmArgs.add("--add-exports");
            jvmArgs.add("javafx.graphics/com.sun.javafx.application=ALL-UNNAMED");
        }

        for (String propKey : System.getProperties().stringPropertyNames()) {
            if (propKey.startsWith("tlauncher.bootstrap.")) {
                String value = Objects.requireNonNull(System.getProperty(propKey), "property \"" + propKey + "\"");

                String arg = "-D" + propKey + "=" + value;
                jvmArgs.add(arg);

                LOGGER.info("Transferring property: {}", arg);
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
            LOGGER.info("Loading arguments from file: {}", externalArgsFile);
            try {
                return loadArgsFromFile(externalArgsFile);
            } catch (IOException e) {
                LOGGER.error("Cannot load arguments from file: {}", externalArgsFile, e);
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
}
