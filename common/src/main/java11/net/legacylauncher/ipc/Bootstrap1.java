package net.legacylauncher.ipc;

import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.TypeRef;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusProperty;
import org.freedesktop.dbus.annotations.Position;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.Variant;

import java.util.List;
import java.util.Map;

/**
 * Bootstrap IPC, version 1
 */
@DBusInterfaceName(Bootstrap1.INTERFACE_NAME)
@DBusProperty(name = "BootstrapRelease", type = Bootstrap1.BootstrapRelease.class, access = DBusProperty.Access.READ)
@DBusProperty(name = "LauncherConfiguration", type = String.class, access = DBusProperty.Access.READ)
@DBusProperty(name = "LauncherArguments", type = Bootstrap1.LauncherArguments.class, access = DBusProperty.Access.READ)
public interface Bootstrap1 extends DBusInterface {
    String INTERFACE_NAME = "net.legacylauncher.Bootstrap1";
    String OBJECT_PATH = "/net/legacylauncher/Bootstrap";

    @Override
    default String getObjectPath() {
        return OBJECT_PATH;
    }

    interface LauncherArguments extends TypeRef<List<String>> {
    }

    // general metadata block

    /**
     * Returns known release notes for given launcher version
     *
     * @param launcherVersion launcher version
     * @return release notes map, where keys are locales of the notes
     */
    Map<String, ReleaseNotes> GetLauncherReleaseNotes(String launcherVersion);

    // general purpose metadata block

    void SetMetadata(String key, Variant<?> value);

    Variant<?> GetMetadata(String key);

    class BootstrapRelease extends Struct {
        @Position(0)
        public final String name;
        @Position(1)
        public final String version;

        public BootstrapRelease(String name, String version) {
            this.name = name;
            this.version = version;
        }
    }

    class ReleaseNotes extends Struct {
        @Position(0)
        public final String title;
        @Position(1)
        public final String body;

        public ReleaseNotes(String title, String body) {
            this.title = title;
            this.body = body;
        }
    }
}
