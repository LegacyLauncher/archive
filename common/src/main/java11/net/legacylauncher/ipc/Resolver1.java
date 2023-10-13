/*
 * Copyright (c) 2023.
 * SPDX-License-Identifier: GPL-3.0-only
 */

package net.legacylauncher.ipc;

import org.freedesktop.dbus.Marshalling;
import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.Tuple;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.Position;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt64;

import java.lang.reflect.Type;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * DNS resolver IPC, version 1
 */
@DBusInterfaceName("net.legacylauncher.Resolver1")
public interface Resolver1 extends DBusInterface {
    String OBJECT_PATH = "/net/legacylauncher/Resolver";

    @Override
    default String getObjectPath() {
        return OBJECT_PATH;
    }

    void Ping();

    ResolveHostnameResult ResolveHostname(Integer interfaceIndex, String name, Integer family, UInt64 flags);

    int AF_UNSPEC = 0;
    int AF_INET = 1;
    int AF_INET6 = 2;

    final class ResolveHostnameResult extends Tuple {
        @Position(0)
        public final List<ResolvedHostname> hostnames;
        @Position(1)
        public final String canonicalName;
        @Position(2)
        public final UInt64 flags;

        public ResolveHostnameResult(List<ResolvedHostname> hostnames, String canonicalName, UInt64 flags) {
            // work around shitty dbus-java marshalling
            if (!((List<?>) hostnames).stream().allMatch(it -> it instanceof ResolvedHostname)) {
                Type[] types = new Type[hostnames.size()];
                Arrays.fill(types, ResolvedHostname.class);
                try {
                    hostnames = Arrays.stream(Marshalling.deSerializeParameters(hostnames.toArray(), types, null))
                            .map(ResolvedHostname.class::cast)
                            .collect(Collectors.toList());
                } catch (Exception e) {
                    throw new RuntimeException("unable to deserialize resolved hostnames properly", e);
                }
            }
            this.hostnames = hostnames;
            this.canonicalName = canonicalName;
            this.flags = flags;
        }

        public List<InetAddress> toInetAddresses() {
            return hostnames.stream().map(this::toInetAddress).filter(Objects::nonNull).collect(Collectors.toList());
        }

        private InetAddress toInetAddress(ResolvedHostname hostname) {
            if (hostname.family == AF_INET) return toInet4Address(hostname);
            if (hostname.family == AF_INET6) return toInet6Address(hostname);
            if (hostname.address.length == 4) return toInet4Address(hostname);
            if (hostname.address.length == 16) return toInet6Address(hostname);
            return null;
        }

        private InetAddress toInet4Address(ResolvedHostname hostname) {
            try {
                return Inet4Address.getByAddress(canonicalName, hostname.address);
            } catch (UnknownHostException e) {
                return null;
            }
        }

        private InetAddress toInet6Address(ResolvedHostname hostname) {
            try {
                return Inet6Address.getByAddress(canonicalName, hostname.address, hostname.interfaceIndex > 0 ? hostname.interfaceIndex : -1);
            } catch (UnknownHostException e) {
                return null;
            }
        }
    }

    final class ResolvedHostname extends Struct {
        @Position(0)
        public final int interfaceIndex;
        @Position(1)
        public final int family;
        @Position(2)
        public final byte[] address;

        public ResolvedHostname(int interfaceIndex, int family, byte[] address) {
            this.interfaceIndex = interfaceIndex;
            this.family = family;
            this.address = address;
        }
    }
}
