package net.legacylauncher.ipc;

import lombok.Data;
import org.freedesktop.dbus.errors.PropertyReadOnly;
import org.freedesktop.dbus.errors.UnknownProperty;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DBusPropertyDispatcher implements Properties {
    private final Map<? extends DBusPropertyRef, ? extends DBusPropertyHandle<?>> properties;

    private DBusPropertyDispatcher(Map<? extends DBusPropertyRef, ? extends DBusPropertyHandle<?>> properties) {
        this.properties = properties;
    }

    public static DBusPropertyDispatcher of(Collection<DBusPropertyHandle<?>> handles) {
        return new DBusPropertyDispatcher(handles.stream().flatMap((h) -> h.getRefs().stream().map((ref) -> new SingleRef(ref, h)))
                .collect(Collectors.toMap(r -> r.ref, r -> r.handle)));
    }

    public static DBusPropertyDispatcher of(DBusPropertyHandle<?>... handles) {
        return of(Arrays.asList(handles));
    }

    private DBusPropertyHandle<?> lookup(String interfaceName, String propertyName) {
        DBusPropertyHandle<?> handle = properties.get(new DBusPropertyRef(interfaceName, propertyName));
        if (handle == null) {
            throw new UnknownProperty("Unknown property '" + propertyName + "' of interface '" + interfaceName + "'");
        }
        return handle;
    }

    @Override
    public <A> A Get(String _interfaceName, String _propertyName) {
        //noinspection unchecked
        return (A) lookup(_interfaceName, _propertyName).getRead().get();
    }

    @Override
    public <A> void Set(String _interfaceName, String _propertyName, A _value) {
        DBusPropertyHandle<?> handle = lookup(_interfaceName, _propertyName);
        Consumer<?> write = handle.getWrite();
        if (write == null) {
            throw new PropertyReadOnly("Property '" + _propertyName + "' is not writable.");
        }
        //noinspection unchecked
        ((Consumer<? super A>) write).accept(_value);
    }

    @Override
    public Map<String, Variant<?>> GetAll(String _interfaceName) {
        return properties.entrySet().stream().filter(e -> Objects.equals(_interfaceName, e.getKey().getInterfaceName()))
                .collect(Collectors.toMap(e -> e.getKey().getPropertyName(),
                        e -> new Variant<>(e.getValue().getRead().get())));
    }

    @Override
    public String getObjectPath() {
        throw new UnsupportedOperationException("getObjectPath not implemented");
    }

    @Data
    private static class SingleRef {
        private final DBusPropertyRef ref;
        private final DBusPropertyHandle<?> handle;
    }
}
