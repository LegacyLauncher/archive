package ru.turikhay.tlauncher.bootstrap.launcher;

import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.tlauncher.bootstrap.meta.LauncherMeta;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.IOException;

public abstract class Launcher {

    private LauncherMeta meta;

    protected Launcher() {
    }

    public abstract LauncherMeta getMeta() throws IOException, JsonSyntaxException;

    protected ToStringBuilder toStringBuilder() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("meta", meta);
    }

    public final String toString() {
        return toStringBuilder().build();
    }

    private final String logPrefix = '[' + getClass().getSimpleName() + ']';
    protected void log(String s) {
        U.log(logPrefix, s);
    }
}
