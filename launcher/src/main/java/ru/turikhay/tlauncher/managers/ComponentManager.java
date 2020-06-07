package ru.turikhay.tlauncher.managers;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.component.ComponentDependence;
import ru.turikhay.tlauncher.component.InterruptibleComponent;
import ru.turikhay.tlauncher.component.LauncherComponent;
import ru.turikhay.tlauncher.component.RefreshableComponent;
import ru.turikhay.util.async.LoopedThread;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ComponentManager {
    private final TLauncher tlauncher;
    private final List<LauncherComponent> components;
    private final ComponentManager.ComponentManagerRefresher refresher;

    public ComponentManager(TLauncher tlauncher) {
        if (tlauncher == null) {
            throw new NullPointerException();
        } else {
            this.tlauncher = tlauncher;
            components = Collections.synchronizedList(new ArrayList());
            refresher = new ComponentManager.ComponentManagerRefresher(this);
            refresher.start();
        }
    }

    public TLauncher getLauncher() {
        return tlauncher;
    }

    public <T extends LauncherComponent> T loadComponent(Class<T> classOfT) {
        if (classOfT == null) {
            throw new NullPointerException();
        } else if (hasComponent(classOfT)) {
            return getComponent(classOfT);
        } else {
            ComponentDependence dependence = classOfT.getAnnotation(ComponentDependence.class);
            if (dependence != null) {
                Class[] var6;
                int var5 = (var6 = dependence.value()).length;

                for (int var4 = 0; var4 < var5; ++var4) {
                    Class requiredClass = var6[var4];
                    rawLoadComponent(requiredClass);
                }
            }

            return rawLoadComponent(classOfT);
        }
    }

    private <T extends LauncherComponent> T rawLoadComponent(Class<T> classOfT) {
        if (classOfT == null) {
            throw new NullPointerException();
        } else if (!LauncherComponent.class.isAssignableFrom(classOfT)) {
            throw new IllegalArgumentException("Given class is not a LauncherComponent: " + classOfT.getSimpleName());
        } else {
            Constructor constructor;
            try {
                constructor = classOfT.getConstructor(ComponentManager.class);
            } catch (Exception var6) {
                throw new IllegalStateException("Cannot get constructor for component: " + classOfT.getSimpleName(), var6);
            }

            Object instance;
            try {
                instance = constructor.newInstance(this);
            } catch (Exception var5) {
                throw new IllegalStateException("Cannot create a new instance for component: " + classOfT.getSimpleName(), var5);
            }

            T component = (T) instance;
            components.add(component);
            return component;
        }
    }

    public <T extends LauncherComponent> T getComponent(Class<T> classOfT) {
        if (classOfT == null) {
            throw new NullPointerException();
        } else {
            Iterator var3 = components.iterator();

            while (var3.hasNext()) {
                LauncherComponent component = (LauncherComponent) var3.next();
                if (classOfT.isInstance(component)) {
                    return (T) component;
                }
            }

            throw new IllegalArgumentException("Cannot find the component!");
        }
    }

    <T extends LauncherComponent> boolean hasComponent(Class<T> classOfT) {
        if (classOfT == null) {
            return false;
        } else {
            Iterator var3 = components.iterator();

            while (var3.hasNext()) {
                LauncherComponent component = (LauncherComponent) var3.next();
                if (classOfT.isInstance(component)) {
                    return true;
                }
            }

            return false;
        }
    }

    public <T> List<T> getComponentsOf(Class<T> classOfE) {
        ArrayList list = new ArrayList();
        if (classOfE == null) {
            return list;
        } else {
            Iterator var4 = components.iterator();

            while (var4.hasNext()) {
                LauncherComponent component = (LauncherComponent) var4.next();
                if (classOfE.isInstance(component)) {
                    list.add(component);
                }
            }

            return list;
        }
    }

    public void startAsyncRefresh() {
        refresher.iterate();
    }

    void startRefresh() {
        Iterator var2 = components.iterator();

        while (var2.hasNext()) {
            LauncherComponent component = (LauncherComponent) var2.next();
            if (component instanceof RefreshableComponent) {
                RefreshableComponent interruptible = (RefreshableComponent) component;
                interruptible.refreshComponent();
            }
        }

    }

    public void stopRefresh() {
        Iterator var2 = components.iterator();

        while (var2.hasNext()) {
            LauncherComponent component = (LauncherComponent) var2.next();
            if (component instanceof InterruptibleComponent) {
                InterruptibleComponent interruptible = (InterruptibleComponent) component;
                interruptible.stopRefresh();
            }
        }

    }

    class ComponentManagerRefresher extends LoopedThread {
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
