package net.legacylauncher.minecraft.launcher.hooks;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.minecraft.launcher.ProcessHook;
import net.legacylauncher.util.OS;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.interfaces.Peer;

import java.util.Optional;

@SuppressWarnings("unused") // multi-release override
@Slf4j
public class GameModeHookLoader {
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
            log.trace("Exception when retrieving gamemode status", e);
            return false;
        }
    }
}
