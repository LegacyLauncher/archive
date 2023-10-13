package net.legacylauncher.dbus.transport.junixsocket;

import org.freedesktop.dbus.connections.config.TransportConfig;
import org.freedesktop.dbus.connections.transports.AbstractUnixTransport;
import org.freedesktop.dbus.exceptions.TransportConfigurationException;
import org.newsclub.net.unix.*;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class JUnixSocketUnixTransport extends AbstractUnixTransport {
    private final AFUNIXSocketAddress unixSocketAddress;
    private AFUNIXSocketChannel socket;
    private AFUNIXServerSocketChannel serverSocket;

    public JUnixSocketUnixTransport(UnixBusAddress address, TransportConfig transportConfig) throws TransportConfigurationException {
        super(address, transportConfig);

        StringBuilder path = new StringBuilder();
        if (address.isAbstract()) {
            if (!AFSocket.supports(AFSocketCapability.CAPABILITY_ABSTRACT_NAMESPACE)) {
                throw new TransportConfigurationException("Abstract unix addresses not supported by current os");
            }
            path.append('\0');
            path.append(address.getAbstract());
        } else if (address.hasPath()) {
            path.append(address.getPath());
        } else {
            throw new TransportConfigurationException("Unix socket url has to specify 'path' or 'abstract'");
        }

        try {
            unixSocketAddress = AFUNIXSocketAddress.of(path.toString().getBytes(Charset.defaultCharset()));
        } catch (SocketException e) {
            throw new TransportConfigurationException("Unable to resolve unix socket address", e);
        }
    }

    @Override
    protected boolean hasFileDescriptorSupport() {
        return AFSocket.supports(AFSocketCapability.CAPABILITY_FILE_DESCRIPTORS) && AFSocket.supports(AFSocketCapability.CAPABILITY_UNSAFE);
    }

    @Override
    protected SocketChannel connectImpl() throws IOException {
        if (getAddress().isListeningSocket()) {
            if (serverSocket == null || !serverSocket.isOpen()) {
                serverSocket = AFUNIXServerSocketChannel.open();
                serverSocket.configureBlocking(true);
                serverSocket.bind(unixSocketAddress);
            }
            socket = serverSocket.accept();
        } else {
            socket = AFUNIXSocketChannel.open();
            socket.configureBlocking(true);
            socket.connect(unixSocketAddress);
        }

        socket.setAncillaryReceiveBufferSize(1024);

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
        return AFSocket.supports(AFSocketCapability.CAPABILITY_ABSTRACT_NAMESPACE);
    }
}
