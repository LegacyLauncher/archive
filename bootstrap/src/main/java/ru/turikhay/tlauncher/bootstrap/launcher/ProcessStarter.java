package ru.turikhay.tlauncher.bootstrap.launcher;

import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.bootstrap.util.OS;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ProcessStarter {

    public static ProcessBuilder startJarProcess(Path dir, Set<Path> classpath, String mainClass, List<String> jvmArgs, List<String> appArgs) throws IOException {
        Objects.requireNonNull(classpath, "classpath");
        Objects.requireNonNull(jvmArgs, "jvmArgs");
        Objects.requireNonNull(appArgs, "appArgs");

        List<String> cmd = new ArrayList<>();
        cmd.add(getJavaExec());
        cmd.addAll(jvmArgs);
        cmd.add("-classpath");
        cmd.add(constructClassPath(classpath));
        cmd.add(mainClass);
        cmd.addAll(appArgs);

        return startProcess(dir, cmd);
    }

    private static ProcessBuilder startProcess(Path dir, List<String> cmd) throws IOException {
        U.log("[ProcessStarter]", "Starting process:", cmd);
        Objects.requireNonNull(dir, "dir");
        Objects.requireNonNull(cmd, "cmd");
        return new ProcessBuilder().command(cmd).directory(dir.toFile());
    }

    private static String getJavaExec() {
        final char separator = File.separatorChar;

        String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
        if (OS.WINDOWS.isCurrent()) {
            path += ".exe";
        }

        return path;
    }

    private static String constructClassPath(Set<Path> classpathList) throws IOException {
        StringBuilder classpathBuilder = new StringBuilder();
        for (Path classpathEntry : classpathList) {
            Objects.requireNonNull(classpathEntry, "classpathEntry");
            if (!Files.exists(classpathEntry)) {
                throw new FileNotFoundException("classpath not found: " + classpathEntry.toAbsolutePath());
            }
            if (classpathBuilder.length() > 0) {
                classpathBuilder.append(File.pathSeparatorChar);
            }
            classpathBuilder.append(classpathEntry.toAbsolutePath());
        }

        return classpathBuilder.toString();
    }

    public static Set<Path> getDefinedClasspath() throws IOException {
        HashSet<Path> set = new HashSet<>();
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        if (systemClassLoader instanceof URLClassLoader) {
            for (URL url : ((URLClassLoader) systemClassLoader).getURLs()) {
                Path systemClasspathEntry;

                try {
                    systemClasspathEntry = Paths.get(url.toURI());
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }

                if (!Files.exists(systemClasspathEntry)) {
                    throw new FileNotFoundException("system classpath not found: " + systemClasspathEntry.toAbsolutePath());
                }

                set.add(systemClasspathEntry);
            }
        } else {
            String classPath = System.getProperty("tlauncher.bootstrap.classpath");
            if (classPath == null) {
                U.log("[WARNING] tlauncher.bootstrap.classpath is not defined");
                return set;
            }
            for (String path : StringUtils.split(classPath, File.pathSeparatorChar)) {
                Path file = Paths.get(path);
                if (!Files.exists(file)) {
                    throw new FileNotFoundException("predefined classpath entry not found: " + path + "(points to: " + file.toAbsolutePath() + ")");
                }
                set.add(file);
            }
        }
        return set;
    }
}
