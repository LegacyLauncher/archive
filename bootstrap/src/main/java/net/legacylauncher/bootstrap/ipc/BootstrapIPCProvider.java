package net.legacylauncher.bootstrap.ipc;

import net.legacylauncher.bootstrap.Bootstrap;
import net.legacylauncher.bootstrap.launcher.LocalLauncher;

public class BootstrapIPCProvider {
    public static BootstrapIPC createIPC(String bootstrapVersion, String[] launcherArgs, Bootstrap bootstrap, LocalLauncher localLauncher) {
        return new BridgedBootstrapIPC(bootstrapVersion, launcherArgs);
    }
}
