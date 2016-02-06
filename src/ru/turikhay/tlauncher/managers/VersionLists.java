package ru.turikhay.tlauncher.managers;

import java.io.IOException;
import net.minecraft.launcher.updater.ExtraVersionList;
import net.minecraft.launcher.updater.LocalVersionList;
import net.minecraft.launcher.updater.OfficialVersionList;
import net.minecraft.launcher.updater.RemoteVersionList;
import ru.turikhay.tlauncher.component.LauncherComponent;
import ru.turikhay.util.MinecraftUtil;

public class VersionLists extends LauncherComponent {
   private final LocalVersionList localList = new LocalVersionList();
   private final RemoteVersionList[] remoteLists = new RemoteVersionList[]{new ExtraVersionList(), new OfficialVersionList()};

   public VersionLists(ComponentManager manager) throws Exception {
      super(manager);
   }

   public LocalVersionList getLocal() {
      return this.localList;
   }

   public void updateLocal() throws IOException {
      this.localList.setBaseDirectory(MinecraftUtil.getWorkingDirectory());
      this.manager.getLauncher().getSettings().set("minecraft.gamedir", this.localList.getBaseDirectory());
   }

   public RemoteVersionList[] getRemoteLists() {
      return this.remoteLists;
   }
}
