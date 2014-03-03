package com.turikhay.tlauncher.component;

import com.turikhay.tlauncher.component.managers.ComponentManager;

public abstract class RefreshableComponent extends LauncherComponent {
   public RefreshableComponent(ComponentManager manager) throws Exception {
      super(manager);
   }

   public boolean refreshComponent() {
      return this.refresh();
   }

   protected abstract boolean refresh();
}
