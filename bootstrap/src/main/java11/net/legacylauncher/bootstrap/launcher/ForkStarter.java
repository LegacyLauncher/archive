package net.legacylauncher.bootstrap.launcher;

import net.legacylauncher.bootstrap.ipc.DBusBootstrapIPC;
import net.legacylauncher.bootstrap.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ForkStarter extends AbstractDBusStarter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForkStarter.class);
    private Process process;

    private ForkStarter(LocalLauncher launcher, DBusBootstrapIPC ipc) {
        super(launcher, ipc);
        ipc.setMetadata("laf_launcher_aware", Boolean.TRUE); // launcher itself should initialize L&F
    }

    @Override
    protected Void execute() throws Exception {
        Future<Void> dbusServerReady = prepareDBusServer();

        List<Path> classpath = buildClassPath(launcher);

        for (Path path : classpath) {
            LOGGER.info("Classpath entry: {}", path);
        }

        ProcessBuilder builder = new ProcessBuilder();
        builder.command().add(lookupJavaBin().toString());
        builder.command().add("-classpath");
        builder.command().add(classpath.stream().map(Path::toString).collect(Collectors.joining(File.pathSeparator)));
        builder.command().add(Objects.requireNonNull(launcher.getMeta().getEntryPoint(), "LocalLauncher entryPoint"));
        builder.command().add(getBusAddress().toString());
        builder.inheritIO();

        dbusServerReady.get();
        process = builder.start();

        process.onExit().thenAccept((result) -> LOGGER.debug("Child process closed with exit code {}", result.exitValue()));

        return null;
    }

    @Override
    public void close() {
        if (process != null) {
            process.destroy();
        }

        super.close();
    }

    public static Task<Void> start(final LocalLauncher launcher, final DBusBootstrapIPC ipc) {
        Objects.requireNonNull(launcher, "LocalLauncher");
        Objects.requireNonNull(ipc, "DBusBootstrapIPC");

        return new ForkStarter(launcher, ipc);
    }

    private static Path lookupJavaBin() {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) throw new IllegalStateException("Unable to determine java home directory");
        Path javaHomePath = Paths.get(javaHome);
        return Stream.of("bin" + File.separator + "java")
                .map(javaHomePath::resolve)
                .filter(Files::isExecutable)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unable to find java executable in " + javaHomePath));
    }
}
