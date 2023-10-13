package net.legacylauncher.minecraft.launcher.hooks;

import net.legacylauncher.minecraft.launcher.ProcessHook;
import net.legacylauncher.util.OS;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.interfaces.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@SuppressWarnings("unused") // multi-release override
public class GameModeHookLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameModeHookLoader.class);

    public static Optional<ProcessHook> tryToCreate() {
        return GameModeHook.tryToCreate();
    }

    public static boolean isAvailable() {
        if (!OS.LINUX.isCurrent()) {
            return false;
        }
        try (DBusConnection session = DBusConnectionBuilder.forSessionBus().build()) {
            Peer peer = session.getRemoteObject("com.feralinteractive.GameMode", "/com/feralinteractive/GameMode", Peer.class);
            peer.Ping();
            return true;
        } catch (Exception e) {
            LOGGER.trace("Exception when retrieving gamemode status", e);
            return false;
        }
    }
}
