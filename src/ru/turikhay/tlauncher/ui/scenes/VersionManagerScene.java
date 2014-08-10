package ru.turikhay.tlauncher.ui.scenes;

import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.versions.VersionHandler;

public class VersionManagerScene extends PseudoScene {
   private static final long serialVersionUID = 758826812081732720L;
   final VersionHandler handler = new VersionHandler(this);

   public VersionManagerScene(MainPane main) {
      super(main);
      this.add(this.handler.list);
   }

   public void onResize() {
      super.onResize();
      this.handler.list.setLocation(this.getWidth() / 2 - this.handler.list.getWidth() / 2, this.getHeight() / 2 - this.handler.list.getHeight() / 2);
   }
}
