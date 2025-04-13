package net.legacylauncher.ipc;

import org.freedesktop.dbus.types.UInt64;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DBusResolverIPC implements ResolverIPC {
    private final Resolver1 resolver;

    public DBusResolverIPC(Resolver1 resolver) {
        this.resolver = resolver;
    }

    @Override
    public String describe() {
        return "bootstrap-backed dbus ipc";
    }

    private CompletableFuture<Resolver1.ResolveHostnameResult> doResolve(String hostname) {
        return CompletableFuture.supplyAsync(() -> resolver.ResolveHostname(0, hostname, Resolver1.AF_UNSPEC, new UInt64(0))).thenApply(Resolver1.ResolveHostnameResult::new);
    }

    @Override
    public CompletableFuture<List<InetAddress>> resolveAddress(String hostname) {
        return doResolve(hostname).thenApply(Resolver1.ResolveHostnameResult::toInetAddresses);
    }

    @Override
    public String resolveCanonicalHostname(String host) throws UnknownHostException {
        try {
            return doResolve(host).thenApply(Resolver1.ResolveHostnameResult::getCanonicalName).get();
        } catch (Exception e) {
            UnknownHostException exception = new UnknownHostException("Cannot resolve canonical hostname for " + host);
            exception.initCause(e);
            throw exception;
        }
    }
}
