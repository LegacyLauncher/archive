package net.legacylauncher.ipc;

import org.apache.http.conn.DnsResolver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface ResolverIPC extends DnsResolver {
    String describe();
    CompletableFuture<List<InetAddress>> resolveAddress(String hostname);

    @Override
    default InetAddress[] resolve(String host) throws UnknownHostException {
        try {
            return resolveAddress(host).get().toArray(new InetAddress[0]);
        } catch (InterruptedException e) {
            UnknownHostException outer = new UnknownHostException("resolve interrupted");
            outer.initCause(e);
            throw outer;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof UnknownHostException) {
                throw (UnknownHostException) cause;
            }
            UnknownHostException outer = new UnknownHostException("resolver execution exception");
            outer.initCause(cause);
            throw outer;
        }
    }
}
