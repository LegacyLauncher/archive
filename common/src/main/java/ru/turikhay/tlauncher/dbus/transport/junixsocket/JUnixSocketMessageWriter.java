package ru.turikhay.tlauncher.dbus.transport.junixsocket;

import org.freedesktop.dbus.exceptions.MarshallingException;
import org.freedesktop.dbus.messages.Message;
import org.freedesktop.dbus.spi.message.IMessageWriter;
import org.freedesktop.dbus.utils.Hexdump;
import org.newsclub.net.unix.AFUNIXSocketChannel;
import org.newsclub.net.unix.FileDescriptorAccess;
import org.newsclub.net.unix.FileDescriptorCast;
import org.newsclub.net.unix.Hack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class JUnixSocketMessageWriter implements IMessageWriter {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AFUNIXSocketChannel socket;

    public JUnixSocketMessageWriter(AFUNIXSocketChannel socket) {
        this.socket = socket;
    }

    @Override
    public void close() throws IOException {
        if (socket.isOpen()) {
            logger.debug("Closing Message Writer");
            socket.close();
        }
    }

    @Override
    public boolean isClosed() {
        return !socket.isOpen();
    }

    @Override
    public void writeMessage(Message msg) throws IOException {
        logger.debug("<= {}", msg);

        if (msg == null) return;

        byte[][] wireData = msg.getWireData();

        if (wireData == null) {
            logger.warn("Message {} wire-data was null!", msg);
            return;
        }

        List<org.freedesktop.dbus.FileDescriptor> fdsList = msg.getFiledescriptors();
        if (!fdsList.isEmpty()) {
            try {
                FileDescriptor[] fds = new FileDescriptor[fdsList.size()];
                for (int i = 0; i < fdsList.size(); i++) {
                    fds[i] = Hack.createFileDescriptor(fdsList.get(i).getIntFileDescriptor());
                }

                socket.setOutboundFileDescriptors(fds);
            } catch (IOException e) {
                logger.error("Unable to extract FileDescriptor", e);
                return;
            }
        }

        for (byte[] bytes : wireData) {
            if (logger.isTraceEnabled()) {
                logger.trace("{}", bytes == null ? "(buffer was null)" : Hexdump.format(bytes));
            }

            if (bytes == null) break;

            socket.write(ByteBuffer.wrap(bytes));
        }

        if (!fdsList.isEmpty()) {
            socket.setOutboundFileDescriptors((FileDescriptor[]) null);
        }

        logger.trace("Message sent: {}", msg);
    }
}
