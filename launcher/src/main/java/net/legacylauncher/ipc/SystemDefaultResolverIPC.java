package net.legacylauncher.ipc;


import org.apache.hc.client5.http.SystemDefaultDnsResolver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class SystemDefaultResolverIPC implements ResolverIPC {
    public static final ResolverIPC INSTANCE = new SystemDefaultResolverIPC();

    private SystemDefaultResolverIPC() {
    }

    @Override
    public String describe() {
        return "system default";
    }

    @Override
    public CompletableFuture<List<InetAddress>> resolveAddress(String hostname) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Arrays.stream(SystemDefaultDnsResolver.INSTANCE.resolve(hostname)).collect(Collectors.toList());
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        return SystemDefaultDnsResolver.INSTANCE.resolve(host);
    }

    @Override
    public String resolveCanonicalHostname(String host) throws UnknownHostException {
        return SystemDefaultDnsResolver.INSTANCE.resolveCanonicalHostname(host);
    }
}
