package ru.turikhay.tlauncher.managers;

import net.minecraft.launcher.updater.ExtraVersionList;
import net.minecraft.launcher.updater.LocalVersionList;
import net.minecraft.launcher.updater.OfficialVersionList;
import net.minecraft.launcher.updater.RemoteVersionList;
import ru.turikhay.tlauncher.component.LauncherComponent;
import ru.turikhay.util.MinecraftUtil;

import java.io.IOException;

public class VersionLists extends LauncherComponent {
    private final LocalVersionList localList = new LocalVersionList();

    private final OfficialVersionList officialVersionList = new OfficialVersionList();
    private final ExtraVersionList extraVersionList = new ExtraVersionList();

    {
        extraVersionList.addDependancy(officialVersionList);
    }

    private final RemoteVersionList[] remoteLists = new RemoteVersionList[]{
            extraVersionList,
            officialVersionList
    };

    public VersionLists(ComponentManager manager) throws Exception {
        super(manager);
    }

    public LocalVersionList getLocal() {
        return localList;
    }

    public void updateLocal() throws IOException {
        localList.setBaseDirectory(MinecraftUtil.getWorkingDirectory());
        manager.getLauncher().getSettings().set("minecraft.gamedir", localList.getBaseDirectory());
    }

    public RemoteVersionList[] getRemoteLists() {
        return remoteLists;
    }
}
