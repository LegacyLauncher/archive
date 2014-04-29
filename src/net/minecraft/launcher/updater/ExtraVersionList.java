package net.minecraft.launcher.updater;

import ru.turikhay.tlauncher.repository.Repository;

public class ExtraVersionList extends RepositoryBasedVersionList {
   public ExtraVersionList() {
      super(Repository.EXTRA_VERSION_REPO);
   }
}
