package ru.turikhay.tlauncher.user.minecraft.strategy.preq;

import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.Validatable;

import java.util.Objects;
import java.util.UUID;

import static ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.Validatable.notEmpty;
import static ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.Validatable.notNull;

public class MinecraftOAuthProfile implements Validatable {
    private UUID id;
    private String name;

    public MinecraftOAuthProfile(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public MinecraftOAuthProfile() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MinecraftOAuthProfile that = (MinecraftOAuthProfile) o;

        if (!Objects.equals(id, that.id)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "MinecraftOAuthProfile{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public void validate() {
        notNull(id, "id");
        notEmpty(name, "name");
    }
}
