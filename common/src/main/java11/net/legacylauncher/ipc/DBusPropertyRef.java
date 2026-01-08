package net.legacylauncher.ipc;

import lombok.Data;

@Data
public class DBusPropertyRef {
    private final String interfaceName;
    private final String propertyName;
}
