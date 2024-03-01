package net.legacylauncher.bootstrap.launcher;

import com.google.gson.JsonSyntaxException;
import net.legacylauncher.bootstrap.json.ToStringBuildable;
import net.legacylauncher.bootstrap.meta.LauncherMeta;

import java.io.IOException;

public abstract class Launcher extends ToStringBuildable {

    protected Launcher() {
    }

    public abstract LauncherMeta getMeta() throws IOException, JsonSyntaxException;
}
