package net.legacylauncher.portals.dbus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;

import java.util.Map;

@DBusInterfaceName("org.freedesktop.portal.Request")
public interface RequestInterface extends DBusInterface {
    void Close();

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    class Response extends DBusSignal {
        UInt32 response;
        Map<String, Variant<?>> results;

        public Response(String path, UInt32 response, Map<String, Variant<?>> results) throws DBusException {
            super(path, response, results);
            this.response = response;
            this.results = results;
        }
    }
}
