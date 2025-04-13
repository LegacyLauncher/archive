/*
 * Copyright (c) 2023.
 * SPDX-License-Identifier: GPL-3.0-only
 */

package net.legacylauncher.ipc;

import org.freedesktop.dbus.Struct;
import org.freedesktop.dbus.Tuple;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.Position;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.UInt64;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * DNS resolver IPC, version 1
 */
@DBusInterfaceName("net.legacylauncher.Resolver1")
public interface Resolver1 extends DBusInterface {
    String OBJECT_PATH = "/net/legacylauncher/Resolver";
    int AF_UNSPEC = 0;
    int AF_INET = 1;
    int AF_INET6 = 2;

    @Override
    default String getObjectPath() {
        return OBJECT_PATH;
    }

    void Ping();

    Triple<List<ResolvedHostname>, String, UInt64> ResolveHostname(int interfaceIndex, String name, int family, UInt64 flags);

    final class Triple<A, B, C> extends Tuple {
        @Position(0)
        public final A first;
        @Position(1)
        public final B second;
        @Position(2)
        public final C third;

        public Triple(A first, B second, C third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }

    final class ResolveHostnameResult {
        private final List<ResolvedHostname> hostnames;
        private final String canonicalName;
        private final UInt64 flags;

        public ResolveHostnameResult(List<ResolvedHostname> hostnames, String canonicalName, UInt64 flags) {
            this.hostnames = hostnames;
            this.canonicalName = canonicalName;
            this.flags = flags;
        }

        public ResolveHostnameResult(Triple<List<ResolvedHostname>, String, UInt64> triple) {
            this(triple.first, triple.second, triple.third);
        }

        public List<InetAddress> toInetAddresses() {
            return getHostnames().stream().map(this::toInetAddress).filter(Objects::nonNull).collect(Collectors.toList());
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
                return Inet4Address.getByAddress(getCanonicalName(), hostname.address);
            } catch (UnknownHostException e) {
                return null;
            }
        }

        private InetAddress toInet6Address(ResolvedHostname hostname) {
            try {
                return Inet6Address.getByAddress(getCanonicalName(), hostname.address, hostname.interfaceIndex > 0 ? hostname.interfaceIndex : -1);
            } catch (UnknownHostException e) {
                return null;
            }
        }

        public List<ResolvedHostname> getHostnames() {
            return this.hostnames;
        }

        public String getCanonicalName() {
            return this.canonicalName;
        }

        public UInt64 getFlags() {
            return this.flags;
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
