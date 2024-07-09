package net.legacylauncher.portals.dbus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;

@DBusInterfaceName("org.freedesktop.portal.Settings")
@DBusProperty(name = "version", type = UInt32.class)
public interface SettingsInterface extends DBusInterface {
    @Deprecated
    <T> T Read(String namespace, String key);

    <T> T ReadOne(String namespace, String key);

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    class SettingChanged extends DBusSignal {
        String namespace;
        String key;
        Variant<?> value;

        public SettingChanged(String _objectPath, String namespace, String key, Variant<?> value) throws DBusException {
            super(_objectPath, namespace, key, value);
            this.namespace = namespace;
            this.key = key;
            this.value = value;
        }
    }
}
