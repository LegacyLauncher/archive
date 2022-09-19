package ru.turikhay.tlauncher.portals.dbus;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.FileDescriptor;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.Variant;

import java.util.Map;

@DBusInterfaceName("org.freedesktop.portal.OpenURI")
public interface OpenURIInterface extends DBusInterface {
    DBusPath OpenDirectory(String parentWindow, FileDescriptor fd, Map<String, Variant<?>> options);

    DBusPath OpenFile(String parentWindow, FileDescriptor fd, Map<String, Variant<?>> options);

    DBusPath OpenURI(String parentWindow, String uri, Map<String, Variant<?>> options);
}
