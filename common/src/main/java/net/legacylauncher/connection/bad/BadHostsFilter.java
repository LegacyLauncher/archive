package net.legacylauncher.connection.bad;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.legacylauncher.connection.Connection;
import net.legacylauncher.connection.ConnectionInfo;
import net.legacylauncher.connection.UrlConnector;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BadHostsFilter<C extends Connection> implements UrlConnector<C> {
    private static final Set<Class<? extends IOException>> EXCEPTIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            UnknownHostException.class,
            SocketTimeoutException.class
    )));

    BadHostsList hostList;
    UrlConnector<C> delegate;

    @Override
    public C connect(ConnectionInfo info) throws IOException {
        URL url = info.getUrl();
        if (hostList.contains(url)) {
            throw new BadHostException(url);
        }
        try {
            return delegate.connect(info);
        } catch (IOException ioE) {
            boolean isBadException = EXCEPTIONS.stream().anyMatch(ex ->
                    ex.isAssignableFrom(ioE.getClass())
            );
            if (isBadException) {
                hostList.add(url);
            }
            throw ioE;
        }
    }
}
