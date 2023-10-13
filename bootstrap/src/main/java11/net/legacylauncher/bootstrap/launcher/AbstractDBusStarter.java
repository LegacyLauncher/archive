package net.legacylauncher.bootstrap.launcher;

import net.legacylauncher.bootstrap.ipc.BootstrapIPC;
import net.legacylauncher.bootstrap.ipc.DBusBootstrapIPC;
import net.legacylauncher.ipc.DBusConnectionForwarder;
import org.freedesktop.dbus.connections.BusAddress;
import org.freedesktop.dbus.connections.impl.DirectConnection;
import org.freedesktop.dbus.connections.impl.DirectConnectionBuilder;
import org.freedesktop.dbus.connections.transports.TransportBuilder;
import org.freedesktop.dbus.utils.Util;
import org.newsclub.net.unix.AFSocket;
import org.newsclub.net.unix.AFSocketCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.concurrent.*;

public abstract class AbstractDBusStarter extends AbstractStarter implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDBusStarter.class);
    protected final LocalLauncher launcher;
    protected final DBusBootstrapIPC ipc;
    private BusAddress busAddress;
    private ExecutorService executorService;

    protected AbstractDBusStarter(LocalLauncher launcher, DBusBootstrapIPC ipc) {
        this.launcher = launcher;
        this.ipc = ipc;

        ipc.addListener(new BootstrapIPC.Listener() {
            @Override
            public void onClosing() {
                close();
            }
        });
    }

    protected BusAddress getBusAddress() {
        if (busAddress == null) {
            busAddress = createDynamicSession(false);
        }
        return busAddress;
    }

    public ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor(this::newThread);
        }
        return executorService;
    }

    private Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(getClass().getSimpleName());
        thread.setDaemon(true);
        return thread;
    }

    protected Future<Void> prepareDBusServer() {
        BusAddress busAddress = getBusAddress();
        CompletableFuture<Void> serverAccepting = new CompletableFuture<>();
        getExecutorService().submit(() -> {
            try {
                DirectConnectionBuilder builder = DirectConnectionBuilder.forAddress(busAddress.getListenerAddress().toString());
                if (busAddress.isBusType("UNIX")) {
                    builder.transportConfig().configureSasl().withAuthMode(TransportBuilder.SaslAuthMode.AUTH_EXTERNAL);
                } else if (busAddress.isBusType("TCP")) {
                    builder.transportConfig().configureSasl().withAuthMode(TransportBuilder.SaslAuthMode.AUTH_COOKIE);
                }
                builder.transportConfig().withPreConnectCallback((transport) -> serverAccepting.complete(null));
                DirectConnection connection = builder.build();
                DBusConnectionForwarder forwarder = new DBusConnectionForwarder.Direct(connection);
                ipc.register(forwarder);
                connection.listen();
            } catch (Exception e) {
                serverAccepting.completeExceptionally(e);
            }
        });
        return serverAccepting;
    }

    @Override
    public void close() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    LOGGER.warn("Starter unable to complete tasks within the given period of time");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void interrupted() {
        ipc.requestClose();
    }

    private static BusAddress createDynamicSession(boolean listeningAddress) {
        if (AFSocket.supports(AFSocketCapability.CAPABILITY_UNIX_DOMAIN)) {
            // let's use abstract unix socket if the host supports it
            return BusAddress.of(createDynamicSessionAddress(listeningAddress, AFSocket.supports(AFSocketCapability.CAPABILITY_ABSTRACT_NAMESPACE)));
        } else {
            // fallback to tcp session (i'm looking at you, windows!)
            return BusAddress.of(TransportBuilder.createDynamicSession("TCP", listeningAddress));
        }
    }

    private static final String ALPHABET = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789";

    // let's do own nice-looking abstract addresses
    private static String createDynamicSessionAddress(boolean listeningSocket, boolean abstractAddress) {
        if (abstractAddress) {
            StringBuilder builder = new StringBuilder("unix:abstract=dbus-");
            for (int i = 0; i < 10; i++) {
                builder.append(ALPHABET.charAt(ThreadLocalRandom.current().nextInt(0, ALPHABET.length())));
            }
            if (listeningSocket) {
                builder.append(",listen=true");
            }
            builder.append(",guid=").append(Util.genGUID());
            return builder.toString();
        }
        return Util.createDynamicSessionAddress(listeningSocket, false);
    }
}
