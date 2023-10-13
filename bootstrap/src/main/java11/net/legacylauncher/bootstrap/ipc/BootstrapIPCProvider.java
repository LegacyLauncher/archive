package net.legacylauncher.bootstrap.ipc;

import net.legacylauncher.bootstrap.Bootstrap;
import net.legacylauncher.bootstrap.launcher.LocalLauncher;
import org.newsclub.net.unix.AFSocket;
import org.newsclub.net.unix.AFSocketCapability;

import java.io.IOException;

@SuppressWarnings("unused") // multi-release override
public class BootstrapIPCProvider {
    public static BootstrapIPC createIPC(String bootstrapVersion, String[] launcherArgs, Bootstrap bootstrap, LocalLauncher localLauncher) throws IOException {
        // Get rid of BridgedBootstrapIPC when dbus-java upgraded to 4.3.1
        if (AFSocket.supports(AFSocketCapability.CAPABILITY_UNIX_DOMAIN) && localLauncher.getMeta().getEntryPoint() != null) {
            return new DBusBootstrapIPC(bootstrapVersion, launcherArgs, bootstrap.isFork());
        }
        return new BridgedBootstrapIPC(bootstrapVersion, launcherArgs);
    }
}
