package net.legacylauncher.bootstrap.launcher;

import com.google.gson.JsonSyntaxException;
import net.legacylauncher.bootstrap.json.ToStringBuildable;
import net.legacylauncher.bootstrap.meta.LauncherMeta;
import net.legacylauncher.bootstrap.util.U;

import java.io.IOException;

public abstract class Launcher extends ToStringBuildable {

    protected Launcher() {
    }

    public abstract LauncherMeta getMeta() throws IOException, JsonSyntaxException;

    private final String logPrefix = '[' + getClass().getSimpleName() + ']';

    protected void log(String s) {
        U.log(logPrefix, s);
    }
}
