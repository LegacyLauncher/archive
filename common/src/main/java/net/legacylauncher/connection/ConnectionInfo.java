package net.legacylauncher.connection;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.net.URL;

@AllArgsConstructor
@Value
public class ConnectionInfo {
    URL url;
    int ordinal;
    int total;
}
