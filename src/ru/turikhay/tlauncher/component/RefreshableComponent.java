package ru.turikhay.tlauncher.component;

import ru.turikhay.tlauncher.managers.ComponentManager;
import ru.turikhay.util.async.AsyncThread;

public abstract class RefreshableComponent extends LauncherComponent {
   public RefreshableComponent(ComponentManager manager) throws Exception {
      super(manager);
   }

   public boolean refreshComponent() {
      return this.refresh();
   }

   public void asyncRefresh() {
      AsyncThread.execute(new Runnable() {
         public void run() {
            RefreshableComponent.this.refresh();
         }
      });
   }

   protected abstract boolean refresh();
}
