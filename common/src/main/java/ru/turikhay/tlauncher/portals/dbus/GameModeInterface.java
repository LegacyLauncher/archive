package ru.turikhay.tlauncher.portals.dbus;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;

@DBusInterfaceName("com.feralinteractive.GameMode")
public interface GameModeInterface extends DBusInterface {
    int RegisterGameByPID(int calledPID, int gamePID);
    int UnregisterGameByPID(int calledPID, int gamePID);
}
