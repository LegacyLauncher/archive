/*
 * Copyright (c) 2023.
 * SPDX-License-Identifier: GPL-3.0-only
 */

package net.legacylauncher.ipc;

import org.freedesktop.dbus.connections.AbstractConnection;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DirectConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.interfaces.DBusSigHandler;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.messages.Message;

import java.io.Closeable;
import java.io.IOException;

public interface DBusConnectionForwarder extends Closeable {
    AbstractConnection getConnection();

    <T extends DBusInterface> T getRemoteObject(String objectPath, Class<T> type) throws DBusException;

    default <T extends DBusInterface> void exportObject(String objectPath, T object) throws DBusException {
        getConnection().exportObject(objectPath, object);
    }

    default <T extends DBusInterface> void exportObject(T object) throws DBusException {
        getConnection().exportObject(object);
    }

    default <T extends DBusSignal> AutoCloseable addSigHandler(Class<T> type, DBusInterface object, DBusSigHandler<T> handler) throws DBusException {
        return getConnection().addSigHandler(type, object, handler);
    }

    default <T extends DBusSignal> AutoCloseable addSigHandler(Class<T> type, DBusSigHandler<T> handler) throws DBusException {
        return getConnection().addSigHandler(type, handler);
    }

    default void sendMessage(Message message) {
        getConnection().sendMessage(message);
    }

    @Override
    default void close() throws IOException {
        getConnection().close();
    }

    final class Direct implements DBusConnectionForwarder {
        private final DirectConnection connection;

        public Direct(DirectConnection connection) {
            this.connection = connection;
        }

        @Override
        public DirectConnection getConnection() {
            return connection;
        }

        @Override
        public <T extends DBusInterface> T getRemoteObject(String objectPath, Class<T> type) throws DBusException {
            return connection.getRemoteObject(objectPath, type);
        }
    }

    final class Bus implements DBusConnectionForwarder {
        private final DBusConnection connection;
        private final String busName;

        public Bus(DBusConnection connection, String busName) {
            this.connection = connection;
            this.busName = busName;
        }

        @Override
        public DBusConnection getConnection() {
            return connection;
        }

        @Override
        public <T extends DBusInterface> T getRemoteObject(String objectPath, Class<T> type) throws DBusException {
            return connection.getRemoteObject(busName, objectPath, type);
        }
    }
}
