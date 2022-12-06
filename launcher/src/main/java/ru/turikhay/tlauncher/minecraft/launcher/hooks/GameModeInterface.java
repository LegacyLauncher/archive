package ru.turikhay.tlauncher.minecraft.launcher.hooks;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;

@DBusInterfaceName("com.feralinteractive.GameMode")
public interface GameModeInterface extends DBusInterface {
    int RegisterGameByPID(int callerPID, int gamePID);
    int UnregisterGameByPID(int callerPID, int gamePID);
}
