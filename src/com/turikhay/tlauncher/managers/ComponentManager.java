package com.turikhay.tlauncher.managers;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.component.ComponentDependence;
import com.turikhay.tlauncher.component.InterruptibleComponent;
import com.turikhay.tlauncher.component.LauncherComponent;
import com.turikhay.tlauncher.component.RefreshableComponent;
import com.turikhay.util.async.LoopedThread;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ComponentManager {
   private final TLauncher tlauncher;
   private final List components;
   private final ComponentManager.ComponentManagerRefresher refresher;

   public ComponentManager(TLauncher tlauncher) {
      if (tlauncher == null) {
         throw new NullPointerException();
      } else {
         this.tlauncher = tlauncher;
         this.components = Collections.synchronizedList(new ArrayList());
         this.refresher = new ComponentManager.ComponentManagerRefresher(this);
         this.refresher.start();
      }
   }

   public TLauncher getLauncher() {
      return this.tlauncher;
   }

   public LauncherComponent loadComponent(Class classOfT) {
      if (classOfT == null) {
         throw new NullPointerException();
      } else if (this.hasComponent(classOfT)) {
         return this.getComponent(classOfT);
      } else {
         ComponentDependence dependence = (ComponentDependence)classOfT.getAnnotation(ComponentDependence.class);
         if (dependence != null) {
            Class[] var6;
            int var5 = (var6 = dependence.value()).length;

            for(int var4 = 0; var4 < var5; ++var4) {
               Class requiredClass = var6[var4];
               this.rawLoadComponent(requiredClass);
            }
         }

         return (LauncherComponent)this.rawLoadComponent(classOfT);
      }
   }

   private Object rawLoadComponent(Class classOfT) {
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

         LauncherComponent component = (LauncherComponent)instance;
         this.components.add(component);
         return instance;
      }
   }

   public LauncherComponent getComponent(Class classOfT) {
      if (classOfT == null) {
         throw new NullPointerException();
      } else {
         Iterator var3 = this.components.iterator();

         while(var3.hasNext()) {
            LauncherComponent component = (LauncherComponent)var3.next();
            if (classOfT.isInstance(component)) {
               return component;
            }
         }

         throw new IllegalArgumentException("Cannot find the component!");
      }
   }

   boolean hasComponent(Class classOfT) {
      if (classOfT == null) {
         return false;
      } else {
         Iterator var3 = this.components.iterator();

         while(var3.hasNext()) {
            LauncherComponent component = (LauncherComponent)var3.next();
            if (classOfT.isInstance(component)) {
               return true;
            }
         }

         return false;
      }
   }

   public List getComponentsOf(Class classOfE) {
      List list = new ArrayList();
      if (classOfE == null) {
         return list;
      } else {
         Iterator var4 = this.components.iterator();

         while(var4.hasNext()) {
            LauncherComponent component = (LauncherComponent)var4.next();
            if (classOfE.isInstance(component)) {
               list.add(component);
            }
         }

         return list;
      }
   }

   public void startAsyncRefresh() {
      this.refresher.iterate();
   }

   void startRefresh() {
      Iterator var2 = this.components.iterator();

      while(var2.hasNext()) {
         LauncherComponent component = (LauncherComponent)var2.next();
         if (component instanceof RefreshableComponent) {
            RefreshableComponent interruptible = (RefreshableComponent)component;
            interruptible.refreshComponent();
         }
      }

   }

   public void stopRefresh() {
      Iterator var2 = this.components.iterator();

      while(var2.hasNext()) {
         LauncherComponent component = (LauncherComponent)var2.next();
         if (component instanceof InterruptibleComponent) {
            InterruptibleComponent interruptible = (InterruptibleComponent)component;
            interruptible.stopRefresh();
         }
      }

   }

   class ComponentManagerRefresher extends LoopedThread {
      private final ComponentManager parent;

      ComponentManagerRefresher(ComponentManager manager) {
         super("ComponentManagerRefresher");
         this.parent = manager;
      }

      protected void iterateOnce() {
         this.parent.startRefresh();
      }
   }
}
