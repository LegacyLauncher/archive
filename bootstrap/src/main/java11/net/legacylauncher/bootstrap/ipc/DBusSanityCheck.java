package net.legacylauncher.bootstrap.ipc;

import net.legacylauncher.bootstrap.launcher.AbstractDBusStarter;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.connections.BusAddress;
import org.freedesktop.dbus.connections.impl.DirectConnection;
import org.freedesktop.dbus.connections.impl.DirectConnectionBuilder;
import org.freedesktop.dbus.connections.transports.TransportBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class DBusSanityCheck {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBusSanityCheck.class);

    public static boolean isSane() {
        ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
            private final ThreadFactory factory = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = factory.newThread(r);
                thread.setDaemon(true);
                return thread;
            }
        });

        try {
            BusAddress busAddress = AbstractDBusStarter.createDynamicSession(false);

            CompletableFuture<Void> serverAccepting = new CompletableFuture<>();
            CompletableFuture<Boolean> pingReceived = new CompletableFuture<>();
            executor.submit(() -> {
                try {
                    DirectConnectionBuilder builder = DirectConnectionBuilder.forAddress(busAddress.getListenerAddress().toString());
                    if (busAddress.isBusType("UNIX")) {
                        builder.transportConfig().configureSasl().withAuthMode(TransportBuilder.SaslAuthMode.AUTH_EXTERNAL);
                    } else if (busAddress.isBusType("TCP")) {
                        builder.transportConfig().configureSasl().withAuthMode(TransportBuilder.SaslAuthMode.AUTH_COOKIE);
                    }
                    builder.transportConfig().withListening(true).withPreConnectCallback((transport) -> {
                        serverAccepting.complete(null);
                    });
                    DirectConnection connection = builder.build();
                    connection.exportObject((Pong) () -> {
                        pingReceived.complete(true);
                    });
                    connection.listen();
                } catch (Exception e) {
                    LOGGER.warn("DBus sanity check failed at setup server stage", e);
                    serverAccepting.completeExceptionally(e);
                }
            });
            return serverAccepting.thenComposeAsync(v -> {
                try {
                    DirectConnection connection = null;
                    for (int i = 0; i < 3; i++) {
                        try {
                            connection = DirectConnectionBuilder.forAddress(busAddress.toString()).build();
                            break;
                        } catch (DBusException e) {
                            // due to shittiness of DBus-Java there is no robust way to wait for listen event
                            // for direct connection (at least in 4.x branch), so that fabulous shit written
                            Thread.sleep(10);
                            LOGGER.debug("DBus sanity check {} failed", i + 1);
                        }
                    }
                    if (connection == null) {
                        return CompletableFuture.completedFuture(false);
                    }
                    Pong pong = connection.getRemoteObject(Pong.OBJECT_PATH, Pong.class);
                    pong.Ping();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return pingReceived;
            }, executor).get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.warn("Failed to check dbus sanity", e);
            return false;
        } finally {
            executor.shutdown();
        }
    }

    @DBusInterfaceName("net.legacylauncher.Pong")
    public interface Pong extends DBusInterface {

        String OBJECT_PATH = "/net/legacylauncher/Pong";

        void Ping();

        @Override
        default String getObjectPath() {
            return OBJECT_PATH;
        }
    }
}
