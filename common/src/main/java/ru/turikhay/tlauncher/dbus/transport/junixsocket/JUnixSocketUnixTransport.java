package ru.turikhay.tlauncher.dbus.transport.junixsocket;

import org.freedesktop.dbus.connections.config.TransportConfig;
import org.freedesktop.dbus.connections.transports.AbstractUnixTransport;
import org.freedesktop.dbus.exceptions.TransportConfigurationException;
import org.newsclub.net.unix.*;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

public class JUnixSocketUnixTransport extends AbstractUnixTransport {
    private final AFUNIXSocketAddress unixSocketAddress;
    private AFUNIXSocketChannel socket;
    private AFUNIXServerSocketChannel serverSocket;

    public JUnixSocketUnixTransport(UnixBusAddress address, TransportConfig transportConfig) throws TransportConfigurationException {
        super(address, transportConfig);

        if (!address.hasPath()) {
            throw new TransportConfigurationException("Native unix socket url has to specify 'path'");
        }

        try {
            unixSocketAddress = AFUNIXSocketAddress.of(address.getPath());
        } catch (SocketException e) {
            throw new TransportConfigurationException("Unable to resolve unix socket address", e);
        }
    }

    @Override
    protected boolean hasFileDescriptorSupport() {
        return AFSocket.supports(AFSocketCapability.CAPABILITY_FILE_DESCRIPTORS);
    }

    @Override
    protected SocketChannel connectImpl() throws IOException {
        if (getAddress().isListeningSocket()) {
            if (serverSocket == null || !serverSocket.isOpen()) {
                serverSocket = AFUNIXServerSocketChannel.open();
                serverSocket.bind(unixSocketAddress);
            }
            socket = serverSocket.accept();
        } else {
            socket = AFUNIXSocketChannel.open(unixSocketAddress);
        }

        socket.setAncillaryReceiveBufferSize(1024);
        socket.configureBlocking(true);

        return socket;
    }

    @Override
    public void close() throws IOException {
        super.close();

        if (socket != null) {
            if (socket.isOpen()) {
                socket.close();
            }
            socket = null;
        }

        if (serverSocket != null) {
            if (serverSocket.isOpen()) {
                serverSocket.close();
            }
            serverSocket = null;
        }
    }

    @Override
    public int getUid(SocketChannel sock) throws IOException {
        if (sock instanceof AFUNIXSocketExtensions) {
            AFUNIXSocketCredentials peerCredentials = ((AFUNIXSocketExtensions) sock).getPeerCredentials();
            return (int) peerCredentials.getUid();
        }

        throw new IllegalArgumentException("Unable to handle unknown socket type: " + sock.getClass());
    }

    @SuppressWarnings("removal")
    @Override
    protected boolean isAbstractAllowed() {
        return false;
    }
}
