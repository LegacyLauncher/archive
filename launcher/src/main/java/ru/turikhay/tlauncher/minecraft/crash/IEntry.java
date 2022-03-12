package ru.turikhay.tlauncher.minecraft.crash;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.util.StringUtil;

import java.util.Objects;

public class IEntry {
    private final CrashManager manager;
    private final String name;

    public IEntry(CrashManager manager, String name) {
        this.manager = Objects.requireNonNull(manager);
        this.name = StringUtil.requireNotBlank(name, "name");
    }

    public final CrashManager getManager() {
        return manager;
    }

    public final String getName() {
        return name;
    }

    public final String toString() {
        return buildToString().build();
    }

    protected ToStringBuilder buildToString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", getName());
    }
}
