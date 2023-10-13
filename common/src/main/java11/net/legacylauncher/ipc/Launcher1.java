package net.legacylauncher.ipc;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.IntrospectionDescription;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;

/**
 * Launcher IPC, version 1
 */
@DBusInterfaceName(Launcher1.INTERFACE_NAME)
@IntrospectionDescription("Bridge to the launcher APIs")
public interface Launcher1 extends DBusInterface {
    String INTERFACE_NAME = "net.legacylauncher.Launcher1";
    String OBJECT_PATH = "/net/legacylauncher/Launcher";

    @Override
    default String getObjectPath() {
        return OBJECT_PATH;
    }

    /**
     * Emits early when launcher is about to start
     */
    @IntrospectionDescription("Emits early when launcher is about to start")
    final class OnBootStarted extends DBusSignal {

        public OnBootStarted(String path) throws DBusException {
            super(path);
        }
    }

    /**
     * Emits when launcher reached some stage in the boot protocol
     */
    @IntrospectionDescription("Emits when launcher reached some stage in the boot protocol")
    final class OnBootProgress extends DBusSignal {

        private final String stepName;
        private final double percentage;

        public OnBootProgress(String path, String stepName, double percentage) throws DBusException {
            super(path, stepName, percentage);
            this.stepName = stepName;
            this.percentage = percentage;
        }

        /**
         * @return current step name
         */
        public String getStepName() {
            return stepName;
        }

        /**
         * @return total boot progress, in range [0,1]
         */
        public double getPercentage() {
            return percentage;
        }
    }

    /**
     * Emits after successful launch
     */
    @IntrospectionDescription("Emits after successful launch")
    final class OnBootSucceeded extends DBusSignal {

        public OnBootSucceeded(String path) throws DBusException {
            super(path);
        }
    }

    /**
     * Emits when a launcher encountered unrecoverable error and cannot continue boot protocol
     */
    @IntrospectionDescription("Emits when a launcher encountered unrecoverable error and cannot continue boot protocol")
    final class OnBootError extends DBusSignal {

        private final String message;

        public OnBootError(String path, String message) throws DBusException {
            super(path, message);
            this.message = message;
        }

        /**
         * @return error message
         */
        public String getMessage() {
            return message;
        }
    }

    @IntrospectionDescription("Emits when a launcher is about to close normally")
    final class OnCloseRequest extends DBusSignal {
        public OnCloseRequest(String path) throws DBusException {
            super(path);
        }
    }
}
