package net.legacylauncher.bootstrap.ipc;

import net.legacylauncher.bootstrap.Bootstrap;
import net.legacylauncher.bootstrap.launcher.LocalLauncher;

import java.io.IOException;

public class BootstrapIPCProvider {
    public static BootstrapIPC createIPC(String bootstrapVersion, String[] launcherArgs, Bootstrap bootstrap, LocalLauncher localLauncher) throws IOException {
        return new BridgedBootstrapIPC(bootstrapVersion, launcherArgs);
    }
}
