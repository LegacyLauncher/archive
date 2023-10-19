package net.legacylauncher.bootstrap.ipc;

import net.legacylauncher.bootstrap.Bootstrap;
import net.legacylauncher.bootstrap.launcher.LocalLauncher;

import java.io.IOException;

@SuppressWarnings("unused") // multi-release override
public class BootstrapIPCProvider {
    public static BootstrapIPC createIPC(String bootstrapVersion, String[] launcherArgs, Bootstrap bootstrap, LocalLauncher localLauncher) throws IOException {
        if (localLauncher.getMeta().getEntryPoint() != null) {
            return new DBusBootstrapIPC(bootstrapVersion, launcherArgs, bootstrap.isFork());
        }
        return new BridgedBootstrapIPC(bootstrapVersion, launcherArgs);
    }
}
