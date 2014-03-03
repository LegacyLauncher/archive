package com.turikhay.tlauncher.component;

import com.turikhay.tlauncher.component.managers.ComponentManager;
import com.turikhay.util.U;

public abstract class LauncherComponent {
   protected final ComponentManager manager;

   public LauncherComponent(ComponentManager manager) throws Exception {
      if (manager == null) {
         throw new NullPointerException();
      } else {
         this.manager = manager;
      }
   }

   protected void log(Object... w) {
      U.log("[" + this.getClass().getSimpleName() + "]", w);
   }
}
