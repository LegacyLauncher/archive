package ru.turikhay.tlauncher.managers;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.component.ComponentDependence;
import ru.turikhay.tlauncher.component.InterruptibleComponent;
import ru.turikhay.tlauncher.component.LauncherComponent;
import ru.turikhay.tlauncher.component.RefreshableComponent;
import ru.turikhay.util.async.LoopedThread;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.util.*;

public class ComponentManager {
    private final TLauncher tlauncher;
    private final Set<Class<? extends LauncherComponent>> loaded = new HashSet<>();
    private final List<LauncherComponent> components;
    private final ComponentManager.ComponentManagerRefresher refresher;

    public ComponentManager(TLauncher tlauncher) {
        if (tlauncher == null) {
            throw new NullPointerException();
        }

        this.tlauncher = tlauncher;
        components = Collections.synchronizedList(new ArrayList<>());
        refresher = new ComponentManager.ComponentManagerRefresher(this);
        refresher.start();
    }

    public TLauncher getLauncher() {
        return tlauncher;
    }

    public <T extends LauncherComponent> T loadComponent(Class<T> classOfT) {
        if (classOfT == null) {
            throw new NullPointerException();
        }

        preloadComponent(classOfT);
        return getComponent(classOfT);
    }

    private <T extends LauncherComponent> void preloadComponent(@Nonnull Class<T> classOfT) {
        if (hasComponent(classOfT)) return;
        loaded.add(classOfT);

        ComponentDependence dependence = classOfT.getAnnotation(ComponentDependence.class);
        if (dependence != null) {
            for (Class<? extends LauncherComponent> requiredClass : dependence.value()) {
                preloadComponent(requiredClass);
            }
        }

        rawLoadComponent(classOfT);
    }

    private <T extends LauncherComponent> void rawLoadComponent(Class<T> classOfT) {
        if (classOfT == null) {
            throw new NullPointerException();
        } else if (!LauncherComponent.class.isAssignableFrom(classOfT)) {
            throw new IllegalArgumentException("Given class is not a LauncherComponent: " + classOfT.getSimpleName());
        } else {
            Constructor<T> constructor;
            try {
                constructor = classOfT.getConstructor(ComponentManager.class);
            } catch (Exception var6) {
                throw new IllegalStateException("Cannot get constructor for component: " + classOfT.getSimpleName(), var6);
            }

            T component;
            try {
                component = constructor.newInstance(this);
            } catch (Exception var5) {
                throw new IllegalStateException("Cannot create a new instance for component: " + classOfT.getSimpleName(), var5);
            }

            components.add(component);
        }
    }

    public <T extends LauncherComponent> T getComponent(Class<T> classOfT) {
        if (classOfT == null) {
            throw new NullPointerException();
        } else {

            for (LauncherComponent component : components) {
                if (classOfT.isInstance(component)) {
                    return classOfT.cast(component);
                }
            }

            throw new IllegalArgumentException("Cannot find the component!");
        }
    }

    <T extends LauncherComponent> boolean hasComponent(Class<T> classOfT) {
        if (classOfT == null) {
            return false;
        }

        for (LauncherComponent component : components) {
            if (classOfT.isInstance(component)) {
                return true;
            }
        }

        for (Class<? extends LauncherComponent> loadedComponent : loaded) {
            if (classOfT.isAssignableFrom(loadedComponent)) {
                return true;
            }
        }

        return false;
    }

    public <T> List<T> getComponentsOf(Class<T> classOfE) {
        if (classOfE == null) {
            return Collections.emptyList();
        }

        List<T> list = new ArrayList<>();

        for (LauncherComponent component : components) {
            if (classOfE.isInstance(component)) {
                list.add(classOfE.cast(component));
            }
        }

        return list;
    }

    public void startAsyncRefresh() {
        refresher.iterate();
    }

    void startRefresh() {

        for (LauncherComponent component : components) {
            if (component instanceof RefreshableComponent) {
                RefreshableComponent interruptible = (RefreshableComponent) component;
                interruptible.refreshComponent();
            }
        }

    }

    public void stopRefresh() {

        for (LauncherComponent component : components) {
            if (component instanceof InterruptibleComponent) {
                InterruptibleComponent interruptible = (InterruptibleComponent) component;
                interruptible.stopRefresh();
            }
        }

    }

    static class ComponentManagerRefresher extends LoopedThread {
        private final ComponentManager parent;

        ComponentManagerRefresher(ComponentManager manager) {
            super("ComponentManagerRefresher");
            parent = manager;
        }

        protected void iterateOnce() {
            parent.startRefresh();
        }
    }
}
