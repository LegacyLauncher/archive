package ru.turikhay.tlauncher.bootstrap.launcher;

import ru.turikhay.tlauncher.bootstrap.bridge.BootBridge;
import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.util.OS;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProcessStarter implements IStarter {
    @Override
    public Task<Void> start(LocalLauncher launcher, BootBridge bridge) {
        throw new Error("NYI");
    }

    public static Process startJarProcess(File dir, Set<File> classpath, String mainClass, List<String> jvmArgs, List<String> appArgs) throws IOException {
        U.requireNotNull(classpath, "classpath");
        U.requireNotNull(jvmArgs, "jvmArgs");
        U.requireNotNull(appArgs, "appArgs");

        List<String> cmd = new ArrayList<String>();
        cmd.add(getJavaExec());
        cmd.addAll(jvmArgs);
        cmd.add("-classpath");
        cmd.add(constructClassPath(classpath));
        cmd.add(mainClass);
        cmd.addAll(appArgs);

        return startProcess(dir, cmd);
    }

    private static Process startProcess(File dir, List<String> cmd) throws IOException {
        U.log("[ProcessStarter]", "Starting process:", cmd);
        U.requireNotNull(dir, "dir");
        U.requireNotNull(cmd, "cmd");
        return new ProcessBuilder().command(cmd).directory(dir).start();
    }

    private static String getJavaExec() {
        final char separator = File.separatorChar;

        String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
        if (OS.WINDOWS.isCurrent()) {
            path += ".exe";
        }

        return path;
    }

    private static String constructClassPath(Set<File> classpathList) throws IOException {
        StringBuilder classpathBuilder = new StringBuilder();
        for(File classpathEntry : classpathList) {
            U.requireNotNull(classpathEntry, "classpathEntry");
            if(!classpathEntry.exists()) {
                throw new FileNotFoundException("classpath not found: " + classpathEntry.getAbsolutePath());
            }
            if(classpathBuilder.length() > 0) {
                classpathBuilder.append(File.pathSeparatorChar);
            }
            classpathBuilder.append(classpathEntry.getAbsolutePath());
        }

        return classpathBuilder.toString();
    }

    public static Set<File> getSystemClasspath() throws IOException {
        HashSet<File> set = new HashSet<File>();
        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        for(URL url : systemClassLoader.getURLs()) {
            File systemClasspathEntry;

            try {
                systemClasspathEntry = new File(url.toURI());
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }

            if(!systemClasspathEntry.exists()) {
                throw new FileNotFoundException("system classpath not found: " + systemClasspathEntry.getAbsolutePath());
            }

            set.add(systemClasspathEntry);
        }
        return set;
    }
}
