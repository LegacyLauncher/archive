package net.legacylauncher.portals.dbus;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;

@DBusInterfaceName("org.freedesktop.portal.Settings")
public interface SettingsInterface extends DBusInterface {
    <T> T Read(String namespace, String key);
}
