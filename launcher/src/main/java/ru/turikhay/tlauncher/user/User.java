package ru.turikhay.tlauncher.user;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.UUID;

public abstract class User {
    public abstract String getUsername();

    public abstract String getDisplayName();

    public abstract UUID getUUID();

    public abstract String getType();

    protected abstract boolean equals(User user);

    public abstract int hashCode();

    public abstract LoginCredentials getLoginCredentials();

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User)) return false;
        User user = (User) obj;
        return getType().equals(user.getType()) && equals(user);
    }

    protected ToStringBuilder toStringBuilder() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("username", getUsername())
                .append("displayName", getDisplayName())
                .append("uuid", getUUID())
                .append("type", getType());
    }

    public final String toString() {
        return toStringBuilder().build();
    }
}
