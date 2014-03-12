package com.turikhay.tlauncher.component;

import com.turikhay.tlauncher.managers.ComponentManager;

public abstract class RefreshableComponent extends LauncherComponent {
   protected RefreshableComponent(ComponentManager manager) throws Exception {
      super(manager);
   }

   public boolean refreshComponent() {
      return this.refresh();
   }

   protected abstract boolean refresh();
}