package net.legacylauncher.ipc;

import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.interfaces.CallbackHandler;
import org.freedesktop.dbus.types.UInt64;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DBusResolverIPC implements ResolverIPC {
    private final DBusConnectionForwarder connection;
    private final Resolver1 resolver;

    public DBusResolverIPC(DBusConnectionForwarder connection, Resolver1 resolver) {
        this.connection = connection;
        this.resolver = resolver;
    }

    @Override
    public String describe() {
        return "bootstrap-backed dbus ipc";
    }

    @Override
    public CompletableFuture<List<InetAddress>> resolveAddress(String hostname) {
        CompletableFuture<Resolver1.ResolveHostnameResult> future = new CompletableFuture<>();
        try {
            connection.getConnection().callWithCallback(resolver, "ResolveHostname", new CallbackHandler<Resolver1.ResolveHostnameResult>() {
                @Override
                public void handle(Resolver1.ResolveHostnameResult _r) {
                    future.complete(_r);
                }

                @Override
                public void handleError(DBusExecutionException _ex) {
                    future.completeExceptionally(_ex);
                }
            }, 0, hostname, Resolver1.AF_UNSPEC, new UInt64(0));
        } catch (DBusExecutionException e) {
            future.completeExceptionally(e);
        }
        return future.thenApply(Resolver1.ResolveHostnameResult::toInetAddresses);
    }
}
