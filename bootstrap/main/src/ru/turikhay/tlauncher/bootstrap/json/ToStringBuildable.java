package ru.turikhay.tlauncher.bootstrap.json;

import shaded.org.apache.commons.lang3.builder.ToStringBuilder;
import shaded.org.apache.commons.lang3.builder.ToStringStyle;

public abstract class ToStringBuildable {
    protected ToStringBuilder toStringBuilder() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public final String toString() {
        return toStringBuilder().build();
    }
}