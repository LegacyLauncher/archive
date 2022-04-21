package ru.turikhay.tlauncher.ui.swing;

import org.apache.commons.lang3.Validate;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.util.SwingUtil;

import java.awt.*;
import java.lang.ref.SoftReference;
import java.util.Objects;

public class DelayedComponent<T extends Component> implements Blockable {

    private final DelayedComponentLoader<T> componentLoader;
    private SoftReference<T> componentRef;

    public DelayedComponent(DelayedComponentLoader<T> componentLoader) {
        this.componentLoader = componentLoader;
    }

    public T get() {
        if (componentRef != null) {
            final T comp = componentRef.get();
            if (comp != null) {
                return comp;
            }
        }
        return SwingUtil.waitAndReturn(this::loadComponent);
    }

    public boolean isLoaded() {
        return componentRef != null && componentRef.get() != null;
    }

    public void load() {
        get();
    }

    private T loadComponent() {
        T comp = Validate.notNull(this.componentLoader.loadComponent());
        this.componentRef = new SoftReference<>(comp);
        this.componentLoader.onComponentLoaded(comp);
        return comp;
    }


    private Object blockObj;

    @Override
    public void block(Object var1) {
        blockObj = var1;
    }

    @Override
    public void unblock(Object var1) {
        if (Objects.equals(blockObj, var1)) {
            blockObj = null;
        }
    }
}
