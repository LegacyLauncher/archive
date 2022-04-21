package ru.turikhay.tlauncher.user.minecraft.strategy.gos;

import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.Validatable;

import java.util.List;
import java.util.Objects;

import static ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.Validatable.notEmpty;
import static ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.Validatable.notNull;

public class MinecraftUserGameOwnershipResponse implements Validatable {
    private List<Item> items;

    public MinecraftUserGameOwnershipResponse(List<Item> items) {
        this.items = items;
    }

    public MinecraftUserGameOwnershipResponse() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MinecraftUserGameOwnershipResponse that = (MinecraftUserGameOwnershipResponse) o;

        return Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return items != null ? items.hashCode() : 0;
    }

    public List<Item> getItems() {
        return items;
    }

    @Override
    public void validate() {
        notNull(items, "items");
        for (Item item : items) {
            item.validate();
        }
    }

    @Override
    public String toString() {
        return "MinecraftUserGameOwnershipResponse{" +
                "items=" + items +
                '}';
    }

    public static class Item implements Validatable {
        private String name;

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Item item = (Item) o;

            return name.equals(item.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return "Item{" +
                    "name='" + name + '\'' +
                    '}';
        }

        @Override
        public void validate() {
            notEmpty(name, "name");
        }
    }
}
