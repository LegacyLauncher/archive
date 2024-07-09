package net.legacylauncher.bootstrap;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.StringArray;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.bootstrap.util.OS;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.stream.Collectors;

public interface BoostrapRestarter {
    int start(Path workingDir, Collection<? extends String> jvmArgs, Collection<? extends Path> classpath, Collection<? extends String> appArgs, boolean debug) throws IOException;

    Logger LOGGER = LoggerFactory.getLogger(BoostrapRestarter.class);

    static BoostrapRestarter create() {
        if (ExecPosixProcessRestarter.isApplicable()) {
            return new ExecPosixProcessRestarter();
        }
        return new ChildProcessRestarter();
    }

    static Set<Path> getDefinedClasspath() throws IOException {
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

                if (!Files.isReadable(systemClasspathEntry)) {
                    throw new FileNotFoundException("system classpath not found: " + systemClasspathEntry.toAbsolutePath());
                }

                set.add(systemClasspathEntry);
            }
        } else {
            String classPath = System.getProperty("tlauncher.bootstrap.classpath");
            if (classPath == null) {
                LOGGER.warn("tlauncher.bootstrap.classpath is not defined");
                return set;
            }
            for (String path : StringUtils.split(classPath, File.pathSeparatorChar)) {
                Path file = Paths.get(path);
                if (!Files.isReadable(file)) {
                    throw new FileNotFoundException("predefined classpath entry not found: " + path + "(points to: " + file.toAbsolutePath() + ")");
                }
                set.add(file);
            }
        }
        return set;
    }

    @Slf4j
    abstract class CmdlineProcessRestarter implements BoostrapRestarter {
        private static Path getJavaExec() {
            Path binPath = Paths.get(System.getProperty("java.home")).resolve("bin");
            Path executable;

            if (OS.WINDOWS.isCurrent()) {
                executable = binPath.resolve("javaw.exe");
                if (Files.isExecutable(executable)) return executable;
                executable = binPath.resolve("java.exe");
            } else {
                executable = binPath.resolve("java");
            }
            if (Files.isExecutable(executable)) return executable;

            throw new IllegalStateException("Unable to find executable java path");
        }

        @Override
        public final int start(Path workingDir, Collection<? extends String> jvmArgs, Collection<? extends Path> classpath, Collection<? extends String> appArgs, boolean debug) throws IOException {
            List<String> cmdline = new ArrayList<>();
            cmdline.add(getJavaExec().toString());
            cmdline.addAll(jvmArgs);
            String classpathString = classpath.stream().map(Path::toAbsolutePath)
                    .peek(path -> {
                        if (!Files.isReadable(path)) {
                            throw new IllegalArgumentException("Classpath entry not found: " + path);
                        }
                    })
                    .map(Path::toString).collect(Collectors.joining(File.pathSeparator));
            if (!classpathString.isEmpty()) {
                cmdline.add("-classpath");
                cmdline.add(classpathString);
            }
            cmdline.addAll(appArgs);

            log.info("Starting process: {}", cmdline);
            return start(workingDir, cmdline, debug);
        }

        protected abstract int start(Path workingDir, List<String> cmdline, boolean debug) throws IOException;
    }

    @Slf4j
    class ChildProcessRestarter extends CmdlineProcessRestarter {
        @Override
        protected int start(Path workingDir, List<String> cmdline, boolean debug) throws IOException {
            Process process = new ProcessBuilder().directory(workingDir.toFile()).command(cmdline).start();
            log.info("Child process started");

            if (debug) {
                try {
                    return process.waitFor();
                } catch (InterruptedException e) {
                    return 1;
                }
            }

            return 0;
        }
    }

    class ExecPosixProcessRestarter extends CmdlineProcessRestarter {
        public static boolean isApplicable() {
            return OS.CURRENT == OS.LINUX || OS.CURRENT == OS.OSX;
        }

        @Override
        protected int start(Path workingDir, List<String> cmdline, boolean debug) throws IOException {
            if (GNUCLibrary.LIBC.chdir(workingDir.toString()) != 0) {
                throw new IOException("Unable to change working directory");
            }
            GNUCLibrary.LIBC.execv(cmdline.get(0), new StringArray(cmdline.toArray(new String[0])));
            throw new IOException("Unable to call execv");
        }

        private interface GNUCLibrary extends Library {
            int chdir(String dir);

            int execv(String path, StringArray args);

            GNUCLibrary LIBC = Native.load("c", GNUCLibrary.class);
        }
    }
}
