package net.legacylauncher.ipc;

import lombok.Getter;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DBusPropertyHandle<T> {
    @Getter
    private final Supplier<T> read;
    @Getter
    private final Consumer<T> write;
    private final Set<DBusPropertyRef> refs = new CopyOnWriteArraySet<>();

    public DBusPropertyHandle(Supplier<T> read, Consumer<T> write) {
        assert read != null;
        this.read = read;
        this.write = write;
    }

    public DBusPropertyHandle(Supplier<T> read) {
        this(read, null);
    }

    public DBusPropertyHandle<T> withRef(DBusPropertyRef ref) {
        refs.add(ref);
        return this;
    }

    public DBusPropertyHandle<T> withRef(String interfaceName, String propertyName) {
        return withRef(new DBusPropertyRef(interfaceName, propertyName));
    }

    public Set<DBusPropertyRef> getRefs() {
        return Collections.unmodifiableSet(refs);
    }
}
