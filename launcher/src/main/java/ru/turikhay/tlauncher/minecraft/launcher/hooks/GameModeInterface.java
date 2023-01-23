package ru.turikhay.tlauncher.minecraft.launcher.hooks;

import org.freedesktop.dbus.FileDescriptor;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;

@DBusInterfaceName("com.feralinteractive.GameMode")
public interface GameModeInterface extends DBusInterface {
    int RegisterGameByPID(int callerPID, int gamePID);
    int RegisterGameByPIDFd(FileDescriptor callerPIDFd, FileDescriptor gamePIDFd);
    int UnregisterGameByPID(int callerPID, int gamePID);
    int UnregisterGameByPIDFd(FileDescriptor callerPIDFd, FileDescriptor gamePIDFd);
}
