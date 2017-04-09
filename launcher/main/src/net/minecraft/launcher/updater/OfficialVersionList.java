package net.minecraft.launcher.updater;

import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.PartialVersion;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;

public class OfficialVersionList extends RemoteVersionList {
    public OfficialVersionList() {
    }

    public RawVersionList getRawList() throws IOException {
        Object lock = new Object();
        Time.start(lock);
        RawVersionList list = gson.fromJson(getUrl("version_manifest.json"), RawVersionList.class);
        Iterator var4 = list.versions.iterator();

        while (var4.hasNext()) {
            PartialVersion version = (PartialVersion) var4.next();
            version.setVersionList(this);
        }

        log("Got in", Time.stop(lock), "ms");
        return list;
    }

    @Override
    public boolean hasAllFiles(CompleteVersion var1, OS var2) {
        return true;
    }

    @Override
    protected InputStreamReader getUrl(String var1) throws IOException {
        return new InputStreamReader(new URL("https://launchermeta.mojang.com/mc/game/" + var1).openConnection(U.getProxy()).getInputStream(), FileUtil.DEFAULT_CHARSET);
    }
}
