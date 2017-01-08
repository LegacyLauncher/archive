package ru.turikhay.tlauncher.minecraft.auth;

import org.apache.commons.lang3.StringUtils;

import java.util.Random;

public class GameProfile {
    public static final GameProfile DEFAULT_PROFILE = new GameProfile("0", "(Default)");
    private final String id;
    private final String name;

    private GameProfile(String id, String name) {
        if (StringUtils.isBlank(id) && StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Name and ID cannot both be blank");
        } else {
            this.id = id;
            this.name = name + new Random().nextInt();
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isComplete() {
        return StringUtils.isNotBlank(getId()) && StringUtils.isNotBlank(getName());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && getClass() == o.getClass()) {
            GameProfile that = (GameProfile) o;
            return !id.equals(that.id) ? false : name.equals(that.name);
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public String toString() {
        return "GameProfile{id=\'" + id + '\'' + ", name=\'" + name + '\'' + '}';
    }
}
