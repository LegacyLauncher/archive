package com.turikhay.tlauncher.component.managers;

import com.turikhay.tlauncher.component.LauncherComponent;
import com.turikhay.util.MinecraftUtil;
import java.io.IOException;
import net.minecraft.launcher.updater.ExtraVersionList;
import net.minecraft.launcher.updater.LocalVersionList;
import net.minecraft.launcher.updater.OfficialVersionList;
import net.minecraft.launcher.updater.RemoteVersionList;

public class VersionLists extends LauncherComponent {
   private final LocalVersionList localList = new LocalVersionList(MinecraftUtil.getWorkingDirectory());
   private final OfficialVersionList officialList = new OfficialVersionList();
   private final ExtraVersionList extraList = new ExtraVersionList();
   private final RemoteVersionList[] remoteLists;

   public VersionLists(ComponentManager manager) throws Exception {
      super(manager);
      this.remoteLists = new RemoteVersionList[]{this.officialList, this.extraList};
   }

   public LocalVersionList getLocal() {
      return this.localList;
   }

   public void updateLocal() throws IOException {
      this.localList.setBaseDirectory(MinecraftUtil.getWorkingDirectory());
   }

   public RemoteVersionList[] getRemoteLists() {
      return this.remoteLists;
   }
}
