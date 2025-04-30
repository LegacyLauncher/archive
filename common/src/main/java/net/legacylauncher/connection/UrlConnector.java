package net.legacylauncher.connection;

import java.io.IOException;

public interface UrlConnector<C extends Connection> {
    C connect(ConnectionInfo info) throws IOException;
}
