package net.legacylauncher.bootstrap.ipc;

import net.legacylauncher.ipc.Resolver1;
import org.freedesktop.dbus.types.UInt64;

import java.util.List;

public class DBusResolverIPC implements Resolver1 {
    @Override
    public void Ping() {
    }

    @Override
    public Triple<List<ResolvedHostname>, String, UInt64> ResolveHostname(int interfaceIndex, String name, int family, UInt64 flags) {
        throw new IllegalArgumentException("No resolving yet, sorry :(");
    }
}
