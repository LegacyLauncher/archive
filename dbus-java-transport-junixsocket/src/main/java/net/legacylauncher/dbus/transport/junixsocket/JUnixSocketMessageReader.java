package net.legacylauncher.dbus.transport.junixsocket;

import org.freedesktop.dbus.FileDescriptor;
import org.freedesktop.dbus.exceptions.DBusConnectionException;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.spi.message.AbstractInputStreamMessageReader;
import org.newsclub.net.unix.AFUNIXSocketChannel;
import org.newsclub.net.unix.FileDescriptorCast;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class JUnixSocketMessageReader extends AbstractInputStreamMessageReader {
    private final AFUNIXSocketChannel socket;
    private final boolean hasFileDescriptorSupport;

    public JUnixSocketMessageReader(AFUNIXSocketChannel socket, boolean hasFileDescriptorSupport) {
        super(socket, hasFileDescriptorSupport);
        this.socket = socket;
        this.hasFileDescriptorSupport = hasFileDescriptorSupport;
    }

    @Override
    protected List<FileDescriptor> readFileDescriptors(SocketChannel _inputChannel) throws DBusException {
        try {
            final List<FileDescriptor> fds;
            if (hasFileDescriptorSupport) {
                java.io.FileDescriptor[] receivedFileDescriptors = socket.getReceivedFileDescriptors();
                if (receivedFileDescriptors.length == 0) {
                    fds = null;
                } else {
                    fds = new ArrayList<>(receivedFileDescriptors.length);
                    for (int i = 0; i < receivedFileDescriptors.length; i++) {
                        fds.set(i, new FileDescriptor(FileDescriptorCast.using(receivedFileDescriptors[i]).as(Integer.class)));
                    }
                }
            } else {
                fds = null;
            }
            return fds;
        } catch (IOException e) {
            throw new DBusConnectionException(e);
        }
    }
}
